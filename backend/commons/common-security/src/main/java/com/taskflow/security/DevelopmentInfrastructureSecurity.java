package com.taskflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** The loopback-only development stack has no production credentials. */
@Configuration
@Profile("!prod & !secure-infrastructure")
public class DevelopmentInfrastructureSecurity {
    @Bean
    SecurityFilterChain developmentInfrastructureFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
    }
}
