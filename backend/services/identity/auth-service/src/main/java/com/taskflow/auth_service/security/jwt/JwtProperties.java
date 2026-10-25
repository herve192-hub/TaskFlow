package com.taskflow.auth_service.security.jwt;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds jwt.* properties loaded through application/config-tree configuration.
 * Issuer and verification material are validated at binding; ProductionSecurity adds RSA-only,
 * key-pair, distribution, and token-lifetime checks for prod. expiration values are milliseconds.
 * The development HMAC secret is a compatibility path, not a production signing-key substitute.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private String publicKey;
    private String privateKey;

    @AssertTrue(message = "An RSA verification key or a development signing key of at least 64 characters is required")
    public boolean isVerificationKeyConfigured() {
        return (publicKey != null && !publicKey.isBlank()) || (secret != null && secret.length() >= 64);
    }

    @NotBlank
    private String issuer;

    private long expiration;

    private long refreshExpiration;
}
