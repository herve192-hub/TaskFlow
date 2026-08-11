
package com.taskflow.user_service.security.jwt;

import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface JwtService {

    String extractUsername(String token);

    List<String> extractRoles(String token);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);

    Claims extractClaims(String token);

    String extractToken(HttpServletRequest request);
}

