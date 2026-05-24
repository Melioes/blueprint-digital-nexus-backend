package com.melioes.blueprintdigitalnexus.controller.chatAi;

import com.melioes.blueprintdigitalnexus.service.chatAi.ZhipuAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI 对话", description = "基础 AI 对话接口")
public class WmsChatController {

    private final ZhipuAiService zhipuAiService;

    @Autowired
    public WmsChatController(ZhipuAiService zhipuAiService) {
        this.zhipuAiService = zhipuAiService;
    }

    /**
     * 基础 AI 对话接口
     * 测试 URL: http://localhost:8080/ai/chat?message=你好
     */

    @GetMapping("/chat")
    @Operation(summary = "基础 AI 对话接口", description = "基础 AI 对话接口")
    public String chat(@RequestParam("message") String message) {
        // 直接交由业务层处理
        return zhipuAiService.callModel(message);
    }
}