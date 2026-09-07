package com.taskflow.project_service.client.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    /**
     * Forward the incoming JWT to downstream services.
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {

        return requestTemplate -> {

            ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            String authorization = requestAttributes == null
                ? null
                : requestAttributes.getRequest()
                    .getHeader( "Authorization" );
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