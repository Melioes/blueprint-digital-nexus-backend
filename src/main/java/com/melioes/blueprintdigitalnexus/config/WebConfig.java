package com.melioes.blueprintdigitalnexus.config;

import com.melioes.blueprintdigitalnexus.interceptor.AdminJwtInterceptor;
import com.melioes.blueprintdigitalnexus.interceptor.UserJwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminJwtInterceptor adminJwtInterceptor;

    @Autowired
    private UserJwtInterceptor userJwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 管理员接口拦截
        registry.addInterceptor(adminJwtInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login",
                        "/admin/auth/register"
                );

        // 普通用户接口拦截
        registry.addInterceptor(userJwtInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register"
                );
    }
}