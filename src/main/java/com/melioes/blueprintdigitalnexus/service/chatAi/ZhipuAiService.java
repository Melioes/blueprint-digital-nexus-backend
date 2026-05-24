package com.melioes.blueprintdigitalnexus.service.chatAi;

public interface ZhipuAiService {
    /**
     * 向智谱 AI 发送单条普通对话消息
     * @param message 用户输入的文本
     * @return AI 回复的文本
     */
    String callModel(String message);
}