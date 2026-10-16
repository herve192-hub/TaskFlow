package com.taskflow.task_service.security.config;

import com.taskflow.task_service.security.jwt.JwtAuthenticationEntryPoint;
import com.taskflow.task_service.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import com.taskflow.security.CookieSecurity;

@Configuration
@org.springframework.context.annotation.Import({com.taskflow.security.ProductionSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint;

    @Value("${TASKFLOW_COOKIE_SECURE:false}")
    private boolean secureCookie;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> CookieSecurity.configure(csrf, secureCookie))

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Health / monitoring
                        .requestMatchers(
                                "/actuator/health", "/actuator/health/**"
                        ).permitAll()

                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").denyAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Everything else requires JWT
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
