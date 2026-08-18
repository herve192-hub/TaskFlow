package com.taskflow.project_service.security.jwt;

public interface JwtService {

    String extractUserId(String token);

    String extractUsername(String token);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);
}