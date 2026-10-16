package com.taskflow.project_service.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import com.taskflow.security.CookieSecurity;

/** Populate account identity and platform authorities; project-specific roles come from memberships. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = CookieSecurity.extractToken(request);

        /*
         * Neither a bearer token nor the configured browser access cookie is present.
         *
         * Continue through the filter chain and let
         * Spring Security decide whether the endpoint
         * requires authentication.
         */
        if (token == null) {

            filterChain.doFilter(request, response);
            return;
        }

        try {

            if (jwtService.isTokenValid(token)
                    && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                String userId =
                        jwtService.extractUserId(token);

                List<SimpleGrantedAuthority> authorities =
                        extractAuthorities(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception exception) {

            /*
             * Invalid JWT should not expose parsing/signature
             * details to the client.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(
            String token
    ) {

        return jwtService
                .extractRoles(token)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
