package com.taskflow.task_service.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        try {

            if (!jwtService.isTokenValid(token)) {

                filterChain.doFilter(request, response);
                return;
            }

            String userId =
                    jwtService.extractUsername(token);

            if (userId == null ||
                    userId.isBlank()) {

                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities =
                    extractAuthorities(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(
            String token
    ) {

        /*
         * Start with ROLE_USER.
         *
         * Once we confirm the exact role claim used by
         * auth-service, we can extract roles from the JWT.
         */

        List<SimpleGrantedAuthority> authorities =
                new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return authorities;
    }
}