package com.melioes.blueprintdigitalnexus.controller.chatAi;

import com.melioes.blueprintdigitalnexus.service.chatAi.ZhipuAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话控制器
 *
 * 提供两个接口：
 * 1. POST /ai/chat — 同步调用，返回完整文本
 * 2. POST /ai/chat/stream — 流式调用，返回 SSE（前端打字机效果）
 *
 * 前端对接：
 * - 登录后的 token 通过 Header 传递（AdminJwtInterceptor 会处理）
 * - 流式接口前端用 fetch + ReadableStream 接收
 */
@Slf4j
@RestController
@RequestMapping("/admin/ai")
@Tag(name = "AI 对话", description = "AI 对话接口")
public class WmsChatController {

    private final ZhipuAiService zhipuAiService;

    @Autowired
    public WmsChatController(ZhipuAiService zhipuAiService) {
        this.zhipuAiService = zhipuAiService;
    }

    /**
     * 同步对话接口
     *
     * 前端调用后等待完整响应，适用于简单场景
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话（同步）", description = "发送消息，等待 AI 完整回复")
    public String chat(@RequestBody ChatRequest request) {
        log.info("[AI 同步] 用户提问: {}", request.getMessage());
        String result = zhipuAiService.callModel(request.getMessage());
        log.info("[AI 同步] AI 回复长度: {}", result.length());
        return result;
    }

    /**
     * 流式对话接口（SSE）
     *
     * 前端通过 fetch + ReadableStream 接收逐字回复
     * produces = TEXT_EVENT_STREAM_VALUE 告诉 Spring 返回 SSE 格式
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 对话（流式）", description = "发送消息，AI 逐字流式回复")
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        log.info("[AI 流式] 用户提问: {}", request.getMessage());
        return zhipuAiService.streamChat(request.getMessage());
    }

    /**
     * 请求体内部类
     */
    public static class ChatRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
