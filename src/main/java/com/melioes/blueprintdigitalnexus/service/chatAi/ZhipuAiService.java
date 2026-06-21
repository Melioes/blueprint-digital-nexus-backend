package com.melioes.blueprintdigitalnexus.service.chatAi;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智谱 AI 服务接口
 */
public interface ZhipuAiService {

    /**
     * 向智谱 AI 发送单条普通对话消息（同步，非流式）
     *
     * @param message 用户输入的文本
     * @return AI 回复的文本
     */
    String callModel(String message);

    /**
     * 流式对话（SSE）
     *
     * 前端通过 SSE 连接接收 AI 逐字回复，实现打字机效果
     *
     * @param message 用户输入的文本
     * @return SseEmitter 前端通过此对象接收流式数据
     */
    SseEmitter streamChat(String message);
}
