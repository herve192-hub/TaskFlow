package com.taskflow.project_service.client.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Forwards the current request's bearer/cookie access credential as a bearer header to user-service.
 * Reading request context per invocation avoids storing one caller's token in a shared bean.
 * FeignErrorDecoder translates access/absence/outage responses; timeouts are configured in
 * shared application configuration. Do not substitute infrastructure Basic credentials here.
 */
@Configuration
public class FeignClientConfig {

    /**
     * Convert bearer or browser-cookie authentication to a downstream Bearer header.
     * Without an active servlet request, no caller credential is attached.
     */
    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {

        return requestTemplate -> {

            ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            String token = requestAttributes == null ? null
                    : com.taskflow.security.CookieSecurity.extractToken(requestAttributes.getRequest());
            String authorization = token == null ? null : "Bearer " + token;
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
