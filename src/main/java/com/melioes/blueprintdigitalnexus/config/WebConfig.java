package com.melioes.blueprintdigitalnexus.config;

import com.melioes.blueprintdigitalnexus.interceptor.AdminJwtInterceptor;
import com.melioes.blueprintdigitalnexus.interceptor.UserJwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

    /**
     * 静态资源映射
     *
     * 让前端能通过 URL 访问到本地磁盘上的图片文件
     * 例如：http://localhost:8443/uploads/avatar/2026/06/11/abc.jpg
     *       → D:/uploads/avatar/2026/06/11/abc.jpg
     *
     * 注意：只有 storage.type=local 时才需要这个映射
     * OSS 模式下前端直接访问 OSS 的 URL，不需要经过后端
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/uploads/");
    }
}