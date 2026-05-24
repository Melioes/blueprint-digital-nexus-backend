package com.melioes.blueprintdigitalnexus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class ZhipuAiConfig {


    @Value("${zhipu.ai.api-key}")
    private  String apiKey;

    @Bean
    public RestTemplate aiRestTemplate() {
        return new RestTemplate();
    }

    public String getApiKey() {
        return apiKey;
    }


}