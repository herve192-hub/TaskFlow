package com.taskflow.project_service.security.config;

import com.taskflow.project_service.security.jwt.JwtAuthenticationEntryPoint;
import com.taskflow.project_service.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import com.taskflow.security.CookieSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@org.springframework.context.annotation.Import({com.taskflow.security.ProductionSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(
        com.taskflow.project_service.security.jwt.JwtProperties.class
)
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

                /*
                 * REST API.
                 */
                .csrf(csrf -> CookieSecurity.configure(csrf, secureCookie))

                /*
                 * JWT authentication is stateless.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Authentication failures.
                 */
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                )

                /*
                 * Public endpoints.
                 */
                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        .requestMatchers(
                                "/actuator/health", "/actuator/health/**"
                        )
                        .permitAll()
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").denyAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        /*
                         * Everything else requires JWT.
                         */
                        .anyRequest()
                        .authenticated()
                )

                /*
                 * JWT filter runs before Spring's username/password
                 * authentication filter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
