package com.springboot.MyTodoList.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myTodoListOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyTodoList API")
                        .description("Spring Boot REST API for Todo List Management")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@mytodolist.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}