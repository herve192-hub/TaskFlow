package com.taskflow.task_service.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Propagates the current request's bearer/cookie access token to project/user internal calls.
 * No token is cached in a singleton field; the request-scoped servlet proxy supplies the caller.
 * FeignErrorDecoder distinguishes access/invalid-reference/outage errors. Non-request/background
 * callers would need their own explicit identity propagation design.
 */
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
