package com.taskflow.project_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

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

        Claims claims = extractAllClaims(token);

        /*
         * The preferred claim should be "userId".
         *
         * If your auth-service currently uses "sub" as the
         * user identifier, this falls back to subject.
         */
        Object userId = claims.get("userId");

        if (userId != null) {
            return userId.toString();
        }

        return claims.getSubject();
    }

    @Override
    public String extractUsername(String token) {

        Claims claims = extractAllClaims(token);

        Object username = claims.get("username");

        if (username != null) {
            return username.toString();
        }

        return claims.getSubject();
    }

    @Override
    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            String issuer = claims.getIssuer();

            if (jwtProperties.getIssuer() != null
                    && !jwtProperties.getIssuer().equals(issuer)) {

                return false;
            }

            return !isTokenExpired(token);

        } catch (Exception exception) {

            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {

        Claims claims = extractAllClaims(token);

        Date expiration = claims.getExpiration();

        return expiration.before(new Date());
    }
}