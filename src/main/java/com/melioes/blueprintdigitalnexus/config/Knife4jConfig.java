package com.melioes.blueprintdigitalnexus.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {


    /**
     * 管理端接口文档分组
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("管理端接口")
                .pathsToMatch("/admin/**")
                .build();
    }

    /**
     * 用户端接口文档分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("用户端接口")
                .pathsToMatch("/user/**")
                .build();
    }
    /**
     * swagger-resources 接口文档分组
     */
//    @Bean
//    public GroupedOpenApi swaggerResourcesApi() {
//        return GroupedOpenApi.builder()
//                .group("swagger-resources")
//                .pathsToMatch("/**")
//                .build();
//    }
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BlueprintDigitalNexus API文档")
                        .version("1.0.0")
                        .description("仓库管理系统 API文档")
                        .contact(new Contact()
                                .name("Melioes")
                                .email("dev@melioes.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}