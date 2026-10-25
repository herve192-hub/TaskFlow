package com.taskflow.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Describes authentication API metadata and the BearerAuth scheme for Springdoc tooling.
 * The description does not authorize requests: SecurityConfig and AuthController enforce access.
 * Production Compose disables documentation endpoints and the security chain denies them;
 * changing this schema does not enable an account-recovery or refresh endpoint.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskflowOpenAPI() {

        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()

                .info(new Info()

                        .title("TaskFlow Auth API")
                        .version("1.0")
                        .description("Authentication Microservice"))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName))

                .components(

                        new Components()

                                .addSecuritySchemes(
                                        securitySchemeName,

                                        new SecurityScheme()

                                                .name(securitySchemeName)

                                                .type(SecurityScheme.Type.HTTP)

                                                .scheme("bearer")

                                                .bearerFormat("JWT")
                                )
                );
    }

}
