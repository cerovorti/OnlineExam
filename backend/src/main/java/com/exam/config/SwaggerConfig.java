package com.exam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("在线考试系统 API文档")
                        .version("1.0.0")
                        .description("在线考试系统后端接口文档")
                        .contact(new Contact().name("考试系统").email("admin@school.edu.cn")));
    }
}