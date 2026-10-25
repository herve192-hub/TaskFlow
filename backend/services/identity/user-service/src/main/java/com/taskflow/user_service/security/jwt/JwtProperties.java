
package com.taskflow.user_service.security.jwt;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds jwt.* verification configuration used by the profile token adapter.
 * Common-security accepts RSA public verification or a sufficiently long local/test HMAC secret.
 * ProductionSecurity rejects private signing material in this service and requires production
 * settings; field presence in this binding object does not grant signing authority.
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

