package com.taskflow.task_service.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/** Forward the request's access token so project/user lookups run with the same caller identity. */
@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final HttpServletRequest httpServletRequest;

    @Bean
    public RequestInterceptor authorizationRequestInterceptor() {

        return requestTemplate -> {

            // Internal HTTP clients forward a Bearer header instead of copying browser cookies.
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
