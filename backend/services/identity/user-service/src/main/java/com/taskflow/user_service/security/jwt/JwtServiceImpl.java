
package com.taskflow.user_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {

        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(
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
    public List<String> extractRoles(String token) {

        Claims claims = extractClaims(token);

        Object roles = claims.get("roles");

        if (roles instanceof List<?> roleList) {

            return roleList.stream()
                    .map(Object::toString)
                    .toList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractClaims(token);

            String issuer = claims.getIssuer();

            return !isTokenExpired(token)
                    && jwtProperties.getIssuer().equals(issuer);

        } catch (Exception exception) {

            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    @Override
    public Claims extractClaims(String token) {

        return extractAllClaims(token);
    }

    @Override
    public String extractToken(HttpServletRequest request) {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            return authorizationHeader.substring(7);
        }

        return null;
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
