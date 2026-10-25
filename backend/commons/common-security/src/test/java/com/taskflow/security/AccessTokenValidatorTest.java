package com.taskflow.security;

import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Security regression tests for signature algorithms, issuer/audience/purpose, and required identity claims.
 * Keys/tokens are generated or constructed as test fixtures, not production credentials. These
 * unit tests exercise the shared parser without starting HTTP services or MongoDB.
 */
class AccessTokenValidatorTest {
    private static final String KEY = "isolated-unit-test-signing-key-".repeat(3);

    private static Map<String, Object> claims() {
        return new HashMap<>(Map.of(
                "sub", "user-1", "iss", "taskflow", "aud", List.of("taskflow-api"),
                "iat", Date.from(Instant.now().minusSeconds(5)),
                "exp", Date.from(Instant.now().plusSeconds(900)),
                "jti", "session-1", "token_use", "access", "email", "user@example.test",
                "roles", List.of("ROLE_USER")));
    }

    private static String sign(Map<String, Object> claims) {
        return Jwts.builder().claims(claims)
                .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512)
                .compact();
    }

    @Test
    void acceptsAnAccessTokenWithTheRequiredIdentityAndPurpose() {
        assertEquals("user-1", AccessTokenValidator.parse(sign(claims()), KEY, "taskflow").getSubject());
    }

    static Stream<String> invalidTokens() {
        Stream<String> missing = Stream.of("sub", "iss", "aud", "iat", "exp", "jti", "token_use", "email", "roles")
                .map(name -> { var claims = claims(); claims.remove(name); return sign(claims); });
        Stream<String> changed = Stream.of(
                Map.entry("iss", "another-issuer"), Map.entry("aud", List.of("another-api")),
                Map.entry("token_use", "refresh"), Map.entry("sub", ""), Map.entry("jti", ""),
                Map.entry("roles", List.of("ROLE_SUPER_ADMIN")), Map.entry("roles", List.of()),
                Map.entry("exp", Date.from(Instant.now().minusSeconds(60))),
                Map.entry("iat", Date.from(Instant.now().plusSeconds(60))))
                .map(change -> { var claims = claims(); claims.put(change.getKey(), change.getValue()); return sign(claims); });
        return Stream.concat(missing, changed);
    }

    @ParameterizedTest
    @MethodSource("invalidTokens")
    void rejectsTokensWithMissingOrInvalidSecurityClaims(String token) {
        assertThrows(RuntimeException.class, () -> AccessTokenValidator.parse(token, KEY, "taskflow"));
    }

    @Test
    void rejectsInvalidSignaturesAndOtherAlgorithms() {
        assertThrows(RuntimeException.class, () -> AccessTokenValidator.parse(sign(claims()), "different-signing-key-".repeat(4), "taskflow"));
        String weakerAlgorithm = Jwts.builder().claims(claims())
                .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
        assertThrows(RuntimeException.class, () -> AccessTokenValidator.parse(weakerAlgorithm, KEY, "taskflow"));
    }

    @Test
    void rejectsUnsignedTokensAndWeakConfiguration() {
        String unsigned = Jwts.builder().claims(claims()).compact();
        assertThrows(RuntimeException.class, () -> AccessTokenValidator.parse(unsigned, KEY, "taskflow"));
        assertThrows(IllegalArgumentException.class, () -> AccessTokenValidator.parse(sign(claims()), "weak-key", "taskflow"));
        assertThrows(IllegalArgumentException.class, () -> AccessTokenValidator.parse(sign(claims()), KEY, ""));
    }
}
