package com.taskflow.task_service.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final HttpServletRequest httpServletRequest;

    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {

        return requestTemplate -> {

            String token = com.taskflow.security.CookieSecurity.extractToken(httpServletRequest);
            String authorization = token == null ? null : "Bearer " + token;

            if (authorization != null
                    && authorization.startsWith( "Bearer " )) {

                requestTemplate.header( "Authorization", authorization );
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}