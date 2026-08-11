package com.taskflow.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI userServiceOpenAPI() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title("TaskFlow User Service API")
                                .version("v1.0")
                                .description("User Management Microservice")

                                .contact(

                                        new Contact()

                                                .name("TaskFlow")
                                                .email("support@taskflow.com")
                                )

                                .license(

                                        new License()

                                                .name("Apache 2.0")
                                )
                )

                .externalDocs(

                        new ExternalDocumentation()

                                .description("TaskFlow Documentation")
                                .url("https://github.com/taskflow")
                );
    }
}