package com.melioes.blueprintdigitalnexus.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

//    // 密钥（后期可以放配置文件）
//    private static final SecretKey KEY =
//            Keys.hmacShaKeyFor("blueprint-nexus-secret-key-123456".getBytes());
//
//    // 过期时间（1天）
//    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    /**
     * 生成 token（支持自定义 claims）
     */
    public static String generateToken(String secret, Long expire, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setClaims(claims) //
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * 解析token
     */
    public static Claims parseToken(String secret, String token) {

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}