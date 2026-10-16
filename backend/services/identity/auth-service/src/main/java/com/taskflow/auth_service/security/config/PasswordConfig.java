package com.taskflow.auth_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.Map;

/** Use Argon2id for new hashes while accepting existing bcrypt hashes during migration. */
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
