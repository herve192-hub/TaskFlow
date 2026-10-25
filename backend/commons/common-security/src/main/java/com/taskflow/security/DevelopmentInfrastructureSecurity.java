package com.taskflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Permits local infrastructure requests only when both prod and secure-infrastructure are absent.
 * ConfigServerApplication and DiscoveryServerApplication import this alongside InfrastructureSecurity;
 * profile selection chooses the appropriate filter chain. The development Compose stack binds
 * infrastructure ports to loopback, so this permissive chain must not be exposed publicly.
 */
@Configuration
@Profile("!prod & !secure-infrastructure")
public class DevelopmentInfrastructureSecurity {
    @Bean
    SecurityFilterChain developmentInfrastructureFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
    }
}
