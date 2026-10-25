package com.taskflow.auth_service.security.jwt;

import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;

/**
 * Token issuance/parsing interface used by AuthServiceImpl and the auth request filter.
 * Access parsing follows common-security's strict contract. Refresh-token issuance exists, but this
 * interface does not provide refresh rotation/exchange or global revocation. Cookie transport is
 * handled through the shared extractor and AuthController rather than by callers persisting tokens.
 */
public interface JwtService {

    String generateAccessToken(UserDetails user);

    String generateRefreshToken(UserDetails user);

    String extractUserId(String token);

    String extractEmail(String token);

    List<String> extractRoles(String token);

    boolean isTokenValid(
            String token,
            UserDetails user
    );

    boolean isTokenExpired(String token);

    Date extractExpiration(String token);

    Claims extractClaims(String token);

    String extractToken(HttpServletRequest request);
}
