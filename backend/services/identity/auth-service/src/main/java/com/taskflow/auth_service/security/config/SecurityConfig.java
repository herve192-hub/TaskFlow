package com.taskflow.auth_service.security.config;

import com.taskflow.auth_service.security.jwt.JwtAuthenticationFilter;
import com.taskflow.auth_service.security.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import com.taskflow.auth_service.security.jwt.JwtAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;
import com.taskflow.security.CookieSecurity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/** Stateless JWT authentication with cookie CSRF protection and one explicit browser origin. */
@Configuration
@org.springframework.context.annotation.Import({com.taskflow.security.ProductionSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${TASKFLOW_WEB_ORIGIN:http://localhost:5173}")
    private String webOrigin;

    @Value("${TASKFLOW_COOKIE_SECURE:false}")
    private boolean secureCookie;

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider
                = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> CookieSecurity.configure(csrf, secureCookie))
                .cors(cors -> cors.configurationSource(
                corsConfigurationSource()
        ))
                .sessionManagement(session
                        -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                )
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                // Preserve the original error status instead of challenging internal error dispatches.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(
                        "/auth/register", "/auth/login", "/auth/csrf",
                        "/actuator/health", "/actuator/health/**"
                )
                .permitAll()
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").denyAll()
                .anyRequest()
                .authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration
                = new CorsConfiguration();

        // Credentialed browser requests require the configured origin, not a wildcard.
        configuration.setAllowedOrigins(List.of(webOrigin));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
                , "X-XSRF-TOKEN"
        ));

        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source
                = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
