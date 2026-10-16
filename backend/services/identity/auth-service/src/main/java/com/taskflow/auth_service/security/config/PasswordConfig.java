package com.taskflow.auth_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.Map;

@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        var legacy = new BCryptPasswordEncoder(12);
        var encoder = new DelegatingPasswordEncoder("argon2", Map.of(
                "argon2", new Argon2PasswordEncoder(16, 32, 1, 19456, 2),
                "bcrypt", legacy));
        encoder.setDefaultPasswordEncoderForMatches(legacy);
        return encoder;
    }
}
