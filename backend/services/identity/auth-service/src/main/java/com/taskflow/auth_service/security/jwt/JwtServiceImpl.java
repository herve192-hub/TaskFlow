package com.taskflow.auth_service.security.jwt;

import com.taskflow.auth_service.security.model.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public String generateAccessToken(UserDetails user) {

        CustomUserDetails customUser =
                requireCustomUserDetails(user);

        Map<String, Object> claims =
                buildClaims(customUser);

        return createToken(
                claims,
                customUser.getId(),
                jwtProperties.getExpiration()
        );
    }

    @Override
    public String generateRefreshToken(UserDetails user) {

        CustomUserDetails customUser =
                requireCustomUserDetails(user);

        Map<String, Object> claims = new HashMap<>();

        claims.put(
                "email",
                customUser.getEmail()
        );

        /*
         * Refresh tokens intentionally carry fewer claims.
         */
        return createToken(
                claims,
                customUser.getId(),
                jwtProperties.getRefreshExpiration()
        );
    }

    @Override
    public String extractUserId(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    @Override
    public String extractEmail(String token) {

        return extractClaims(token)
                .get(
                        "email",
                        String.class
                );
    }

    @Override
    public List<String> extractRoles(String token) {

        Object roles =
                extractClaims(token).get("roles");

        if (!(roles instanceof List<?> roleList)) {
            return List.of();
        }

        return roleList.stream()
                .map(Object::toString)
                .toList();
    }

    @Override
    public boolean isTokenValid(
            String token,
            UserDetails user
    ) {

        try {

            CustomUserDetails customUser =
                    requireCustomUserDetails(user);

            String userId =
                    extractUserId(token);

            String email =
                    extractEmail(token);

            String issuer =
                    extractClaims(token).getIssuer();

            return customUser.getId().equals(userId)
                    && customUser.getEmail().equals(email)
                    && jwtProperties
                            .getIssuer()
                            .equals(issuer)
                    && !isTokenExpired(token);

        } catch (Exception exception) {

            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    @Override
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractToken(
            HttpServletRequest request
    ) {

        String header =
                request.getHeader("Authorization");

        if (header != null
                && header.startsWith("Bearer ")) {

            return header.substring(7);
        }

        return null;
    }

    private Map<String, Object> buildClaims(
            CustomUserDetails user
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "email",
                user.getEmail()
        );

        claims.put(
                "roles",
                user.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );

        return claims;
    }

    private String createToken(
            Map<String, Object> claims,
            String subject,
            long expiration
    ) {

        Date now = new Date();

        Date expiresAt =
                new Date(
                        now.getTime() + expiration
                );

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        return resolver.apply(
                extractClaims(token)
        );
    }

    private CustomUserDetails requireCustomUserDetails(
            UserDetails user
    ) {

        if (!(user instanceof CustomUserDetails customUser)) {

            throw new IllegalArgumentException(
                    "Expected CustomUserDetails"
            );
        }

        return customUser;
    }
}