package com.melioes.blueprintdigitalnexus.service.impl.chatAi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melioes.blueprintdigitalnexus.config.properties.ZhipuAiProperties;
import com.melioes.blueprintdigitalnexus.service.chatAi.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 智谱 AI 服务实现
 *
 * 核心设计：
 * - callModel(): 同步调用，返回完整文本（兼容旧接口）
 * - streamChat(): 流式调用，通过 SseEmitter 逐字推送给前端
 *
 * 流式原理：
 * 1. 构造请求体时设置 stream=true
 * 2. 智谱 API 返回 SSE 格式的流式响应（每行 data: {...}）
 * 3. 逐行读取，解析 delta.content，通过 SseEmitter.send() 推送给前端
 * 4. 收到 [DONE] 时调用 SseEmitter.complete() 结束
 *
 * 关键注意事项（来自 SSE 最佳实践）：
 * - SseEmitter.send(String) 不要用 .event().name()，否则前端解析失败
 * - 过滤空 chunk（AI 模型在"思考"时会发送空内容）
 * - 检查 JSON null 值（has("content") 对 null 也返回 true）
 */
@Slf4j
@Service
public class ZhipuAiServiceImpl implements ZhipuAiService {

    private final ZhipuAiProperties properties;
    private final RestTemplate aiRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 线程池：SSE 流式调用需要在独立线程中执行（不能阻塞 Tomcat 请求线程）
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ZhipuAiServiceImpl(ZhipuAiProperties properties,
                               @Qualifier("aiRestTemplate") RestTemplate aiRestTemplate) {
        this.properties = properties;
        this.aiRestTemplate = aiRestTemplate;
    }

    // ==================== 同步调用（兼容旧接口）====================

    @Override
    public String callModel(String message) {
        try {
            // 构造请求体（OpenAI 兼容格式，智谱也支持）
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", message))
            );

            HttpHeaders headers = buildHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = aiRestTemplate.postForEntity(
                    properties.getApiUrl(), entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    return choices.get(0).path("message").path("content").asText("暂无回复");
                }
            }
            return "AI 响应解析为空";
        } catch (Exception e) {
            log.error("智谱 AI 同步调用失败: {}", e.getMessage(), e);
            return "AI 调用失败：" + e.getMessage();
        }
    }

    // ==================== 流式调用（SSE）====================

    @Override
    public SseEmitter streamChat(String message) {
        // 设置较长超时（AI 生成可能需要 30-60 秒）
        SseEmitter emitter = new SseEmitter(120000L);

        // 在独立线程中执行流式调用，避免阻塞 Tomcat 线程
        executor.execute(() -> {
            try {
                doStreamChat(message, emitter);
            } catch (Exception e) {
                log.error("AI 流式调用异常: {}", e.getMessage(), e);
                try {
                    emitter.send("AI 调用失败：" + e.getMessage());
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        // 注册超时和完成回调
        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> log.debug("SSE 连接正常关闭"));

        return emitter;
    }

    /**
     * 实际执行流式调用
     *
     * 智谱 API 流式响应格式（SSE）：
     * data: {"choices":[{"delta":{"content":"你"},"index":0,...}]}
     * data: {"choices":[{"delta":{"content":"好"},"index":0,...}]}
     * data: [DONE]
     */
    private void doStreamChat(String message, SseEmitter emitter) throws Exception {
        // 1. 构造请求体（关键：stream=true 开启流式）
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "stream", true,
                "messages", List.of(
                        // System prompt：让 AI 用 Markdown 格式回答，并标识身份
                        Map.of("role", "system", "content",
                                "你是智谱 GLM-4-Flash 模型，由智谱 AI 开发。"
                                        + "用结构化 Markdown 格式回答：## 标题分隔主题，"
                                        + "- 列表罗列要点，```language 代码块展示代码，"
                                        + "`行内代码` 标记关键词。简洁有条理。"
                                        + "代码示例要完整可运行。"),
                        Map.of("role", "user", "content", message)
                )
        );

        HttpHeaders headers = buildHeaders();
        // 流式响应需要 text/event-stream
        headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 2. 发起流式请求
        // 注意：RestTemplate 的 postForEntity 会等整个响应完成
        // 为了流式读取，我们需要用 execute 方法 + ResponseExtractor
        RestTemplate streamTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout());
        factory.setReadTimeout((int) properties.getReadTimeout());
        streamTemplate.setRequestFactory(factory);

        streamTemplate.execute(
                properties.getApiUrl(),
                HttpMethod.POST,
                request -> {
                    // 设置请求头
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getHeaders().setBearerAuth(properties.getApiKey());
                    request.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                    // 写入请求体
                    request.getBody().write(objectMapper.writeValueAsBytes(body));
                },
                response -> {
                    // 流式读取响应
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            // 跳过空行
                            if (line.isBlank()) continue;

                            // 解析 SSE 格式：data: {...} 或 data:{...}
                            String data = null;
                            if (line.startsWith("data: ")) {
                                data = line.substring(6);
                            } else if (line.startsWith("data:")) {
                                data = line.substring(5);
                            }

                            if (data == null) continue;

                            // 流结束标记
                            if (data.trim().equals("[DONE]")) {
                                log.debug("AI 流式响应完成");
                                break;
                            }

                            // 解析 JSON，提取 delta.content
                            try {
                                JsonNode root = objectMapper.readTree(data);
                                JsonNode choices = root.path("choices");
                                if (choices.isArray() && !choices.isEmpty()) {
                                    JsonNode delta = choices.get(0).path("delta");
                                    JsonNode contentNode = delta.path("content");

                                    // 关键：过滤空内容和 null 值
                                    if (!contentNode.isMissingNode()
                                            && !contentNode.isNull()
                                            && !contentNode.asText().isBlank()) {
                                        String text = contentNode.asText();
                                        // 通过 SseEmitter 推送给前端
                                        // 注意：直接 send(String)，不要用 .event().name()
                                        emitter.send(text);
                                    }
                                }
                            } catch (Exception parseEx) {
                                // JSON 解析失败（可能是非标准格式），跳过
                                log.debug("跳过无法解析的 SSE 行: {}", line);
                            }
                        }
                    }

                    // 流读取完毕，关闭 SSE 连接
                    emitter.complete();
                    return null;
                }
        );
    }

    /**
     * 构造请求头（同步和流式共用）
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());
        return headers;
    }
}
