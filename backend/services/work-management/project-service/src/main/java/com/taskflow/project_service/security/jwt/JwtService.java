package com.taskflow.project_service.security.jwt;

import java.util.List;

public interface JwtService {

    /**
     * Extracts the canonical TaskFlow user ID
     * from the JWT subject ("sub").
     */
    String extractUserId(String token);

    /**
     * Extracts the user's email from the JWT.
     */
    String extractEmail(String token);

    /**
     * Extracts TaskFlow global roles such as
     * ROLE_USER and ROLE_ADMIN.
     */
    List<String> extractRoles(String token);

    /**
     * Validates the JWT signature, issuer,
     * expiration, and required claims.
     */
    boolean isTokenValid(String token);

    /**
     * Determines whether the JWT has expired.
     */
    boolean isTokenExpired(String token);
}