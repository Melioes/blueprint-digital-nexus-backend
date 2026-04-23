package com.melioes.blueprintdigitalnexus.config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.melioes.blueprintdigitalnexus.mapper")
public class MyBatisPlusConfig {
}
