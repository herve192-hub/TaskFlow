package com.taskflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

/** Basic authentication for Config Server and Eureka in production and secure CI stacks. */
@Configuration
@Profile({"prod", "secure-infrastructure"})
public class InfrastructureSecurity {
    @Bean
    UserDetailsService infrastructureUsers(Environment environment) {
        String username = environment.getRequiredProperty("spring.security.user.name");
        String password = environment.getRequiredProperty("spring.security.user.password");
        if (username.isBlank() || password.length() < 32) {
            throw new IllegalStateException("Infrastructure requires an explicit user and a long random password");
        }
        // Keep an encoded verifier in memory rather than storing the supplied plaintext password.
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password("{bcrypt}" + new BCryptPasswordEncoder(12).encode(password)).roles("INFRA").build());
    }

    @Bean
    SecurityFilterChain infrastructureFilterChain(HttpSecurity http) throws Exception {
        // Eureka registration/heartbeat clients use Basic auth and do not implement browser CSRF.
        return http.csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults()).build();
    }
}
