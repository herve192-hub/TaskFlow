package com.taskflow.auth_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.Map;

/**
 * Defines the PasswordEncoder consumed by authentication, registration, and administrator bootstrap.
 * New hashes use Argon2id; the delegating encoder also accepts prefixed and legacy unprefixed bcrypt.
 * AuthServiceImpl upgrades an old verifier only after a successful credential check. Do not log
 * plaintext passwords or silently remove the legacy matcher while stored accounts still depend on it.
 */
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        var legacy = new BCryptPasswordEncoder(12);
        var encoder = new DelegatingPasswordEncoder("argon2", Map.of(
                // Salt bytes, hash bytes, parallelism, memory KiB (19 MiB), and iterations.
                "argon2", new Argon2PasswordEncoder(16, 32, 1, 19456, 2),
                "bcrypt", legacy));
        // Older stored hashes may have no {bcrypt} prefix; successful login upgrades them.
        encoder.setDefaultPasswordEncoderForMatches(legacy);
        return encoder;
    }
}
