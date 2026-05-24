package com.melioes.blueprintdigitalnexus.service.impl.chatAi;

import com.melioes.blueprintdigitalnexus.config.ZhipuAiConfig;
import com.melioes.blueprintdigitalnexus.service.chatAi.ZhipuAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZhipuAiServiceImpl implements ZhipuAiService {

    private final ZhipuAiConfig zhipuAiConfig;
    private final RestTemplate aiRestTemplate;

    @Autowired
    public ZhipuAiServiceImpl(ZhipuAiConfig zhipuAiConfig, RestTemplate aiRestTemplate) {
        this.zhipuAiConfig = zhipuAiConfig;
        this.aiRestTemplate = aiRestTemplate;
    }

    @Override
    public String callModel(String message) {
        // 调试：先看环境变量有没有读到
//        System.out.println("===== 系统环境变量 ZHIPU_API_KEY =====");
//        System.out.println(System.getenv("ZHIPU_API_KEY"));
//
//        // 再打印配置里的 apiKey
//        System.out.println("===== 注入的 apiKey =====");
//        System.out.println(zhipuAiConfig.getApiKey());
        try {
            String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + zhipuAiConfig.getApiKey());

            // 2. 组装请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "glm-4-flash");
//            requestBody.put("model", "glm-4.5-air");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 发送请求
            ResponseEntity<Map> response = aiRestTemplate.postForEntity(url, entity, Map.class);

            // 4. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map responseMessage = (Map) firstChoice.get("message");
                    return (String) responseMessage.get("content");
                }
            }
            return "解析智谱响应失败";
        } catch (Exception e) {
            e.printStackTrace();
            return "智谱 AI 调用失败：" + e.getMessage();
        }
    }
}