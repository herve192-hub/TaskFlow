package com.taskflow.project_service.client.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final HttpServletRequest httpServletRequest;
    /**
     * Forward the incoming JWT to downstream services.
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {

        return requestTemplate -> {

            String authorization =
                    httpServletRequest.getHeader( "Authorization" );
            if (authorization != null
                    && authorization.startsWith("Bearer ")) {
                requestTemplate.header(
                        "Authorization", authorization );
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}