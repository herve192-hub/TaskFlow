package com.taskflow.auth_service.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {

    String generateAccessToken(UserDetails user);

    String generateRefreshToken(UserDetails user);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails user);

    boolean isTokenExpired(String token);

    Date extractExpiration(String token);

    Claims extractClaims(String token);

    String extractToken(HttpServletRequest request);
}
