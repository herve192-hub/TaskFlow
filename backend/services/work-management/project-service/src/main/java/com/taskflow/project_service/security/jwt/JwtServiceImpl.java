package com.taskflow.project_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUserId(String token) {
        return extractAllClaims(token)
                .getSubject();
    }

    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token)
                .get("email", String.class);
    }

    @Override
    public List<String> extractRoles(String token) {

        Object roles = extractAllClaims(token)
                .get("roles");

        if (!(roles instanceof List<?> roleList)) {
            return List.of();
        }

        return roleList.stream()
                .map(Object::toString)
                .toList();
    }

    @Override
    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            String userId = claims.getSubject();
            String issuer = claims.getIssuer();
            Date expiration = claims.getExpiration();

            if (userId == null || userId.isBlank()) {
                return false;
            }

            if (issuer == null ||
                    !issuer.equals(jwtProperties.getIssuer())) {
                return false;
            }

            if (expiration == null ||
                    expiration.before(new Date())) {
                return false;
            }

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {

        Date expiration =
                extractAllClaims(token)
                        .getExpiration();

        return expiration == null ||
                expiration.before(new Date());
    }
}