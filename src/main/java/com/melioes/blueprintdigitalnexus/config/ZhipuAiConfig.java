package com.melioes.blueprintdigitalnexus.config;

import com.melioes.blueprintdigitalnexus.config.properties.ZhipuAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 智谱 AI 配置类
 *
 * 职责：
 * 1. 注册 ZhipuAiProperties Bean（@ConfigurationProperties 自动绑定 yml）
 * 2. 创建专用 RestTemplate（带超时配置，避免接口卡死）
 *
 * 为什么单独创建 RestTemplate：
 * - 全局 RestTemplate 可能被其他模块使用，超时配置不适合共享
 * - AI 调用需要较长的读取超时（SSE 流式可能持续 30-60 秒）
 */
@Configuration
@EnableConfigurationProperties(ZhipuAiProperties.class)
public class ZhipuAiConfig {

    /**
     * AI 专用 RestTemplate（带超时）
     *
     * connectTimeout: 建立连接的超时，5秒足够
     * readTimeout: 读取响应的超时，SSE 流式需要 60 秒
     */
    @Bean
    public RestTemplate aiRestTemplate(ZhipuAiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout());
        factory.setReadTimeout((int) properties.getReadTimeout());
        return new RestTemplate(factory);
    }
}
