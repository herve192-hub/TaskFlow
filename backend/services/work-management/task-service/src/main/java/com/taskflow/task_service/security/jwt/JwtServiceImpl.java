package com.taskflow.task_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtProperties
                        .getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    @Override
    public boolean isTokenValid(
            String token,
            org.springframework.security.core.userdetails.UserDetails userDetails
    ) {

        try {

            String username = extractUsername(token);

            return username != null &&
                    username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token);

        } catch (Exception exception) {

            return false;
        }
    }

    @Override
    public boolean isTokenValid(String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception exception) {

            return false;
        }
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new java.util.Date());
    }

    private java.util.Date extractExpiration(
            String token
    ) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims = Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }
}