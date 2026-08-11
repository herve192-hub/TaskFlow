// package com.taskflow.user_service.config;

// import com.taskflow.user_service.security.jwt.JwtAuthenticationFilter;
// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtFilter;

//     @Bean
//     SecurityFilterChain securityFilterChain(HttpSecurity http)
//             throws Exception {

//         http

//                 .csrf(csrf -> csrf.disable())

//                 .sessionManagement(session ->
//                         session.sessionCreationPolicy(
//                                 SessionCreationPolicy.STATELESS))

//                 .authorizeHttpRequests(auth -> auth

//                         .requestMatchers(

//                                 "/swagger-ui/**",
//                                 "/v3/api-docs/**",
//                                 "/actuator/**"

//                         ).permitAll()

//                         .anyRequest().authenticated())

//                 .addFilterBefore(
//                         jwtFilter,
//                         UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }
// }

