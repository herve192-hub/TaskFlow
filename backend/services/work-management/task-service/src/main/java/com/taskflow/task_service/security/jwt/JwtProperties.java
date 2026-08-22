package com.taskflow.task_service.security.jwt;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    private String issuer;

    private long expiration;

    private long refreshExpiration;
}