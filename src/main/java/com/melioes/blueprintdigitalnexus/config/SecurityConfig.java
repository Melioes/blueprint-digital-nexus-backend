package com.melioes.blueprintdigitalnexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
// 1. 关闭 csrf
        http.csrf(csrf -> csrf.disable())

                // 2. 关闭 session（关键）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                        )
                )

                // 3. 所有请求放行
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                )

                // 4. 关闭默认登录机制（关键）
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}