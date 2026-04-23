package com.melioes.blueprintdigitalnexus.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
//读取配置文件yml → 找到 jwt 前缀 → 填充到 JwtProperties 对象
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 过期时间（毫秒）
     */
    private Long ttl;

    /**
     * token请求头名称
     */
    private String tokenName;
}