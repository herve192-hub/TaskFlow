// package com.taskflow.auth_service.security.jwt;

// import lombok.Getter;
// import lombok.Setter;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// @ConfigurationProperties(prefix = "jwt")
// @Getter
// @Setter
// public class JwtProperties {

//     private String secret;

//     private long expiration;

//     private long refreshExpiration;

// }


package com.taskflow.auth_service.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret used to sign JWT tokens.
     */
    private String secret;

    /**
     * Access token validity (milliseconds)
     */
    private long expiration;

    /**
     * Refresh token validity (milliseconds)
     */
    private long refreshExpiration;
}