
package com.taskflow.user_service.security.jwt;

import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Verification/claim-access boundary used by profile-service request authentication.
 * The caller identity is the canonical token subject (despite the method name extractUsername), not a profile document ID or requested path user.
 * Implementations delegate the security contract to common-security; this service does not issue
 * credential-account tokens or implement refresh exchange.
 */
public interface JwtService {

    String extractUsername(String token);

    List<String> extractRoles(String token);

    boolean isTokenValid(String token);

    boolean isTokenExpired(String token);

    Claims extractClaims(String token);

    String extractToken(HttpServletRequest request);
}

