package com.taskflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** The same authentication contract is enforced by every business service. */
public final class AccessTokenValidator {
    public static final String AUDIENCE = "taskflow-api";

    private AccessTokenValidator() { }

    public static Claims parse(String token, String secret, String issuer) {
        return parse(token, secret, issuer, null);
    }

    public static Claims parse(String token, String secret, String issuer, String publicKey) {
        boolean asymmetric = publicKey != null && !publicKey.isBlank();
        if (!asymmetric && (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 64)) {
            throw new IllegalArgumentException("JWT signing key must contain at least 64 bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer is required");
        }
        var parser = Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(AUDIENCE)
                .require("token_use", "access");
        if (asymmetric) {
            parser.verifyWith(JwtKeys.publicKey(publicKey)).sig().clear().add(Jwts.SIG.RS256).and();
        } else {
            parser.verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .sig().clear().add(Jwts.SIG.HS512).and();
        }
        var signed = parser.build().parseSignedClaims(token);
        Claims claims = signed.getPayload();
        Date now = new Date();
        Object roles = claims.get("roles");
        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getId() == null || claims.getId().isBlank()
                || claims.getExpiration() == null || !claims.getExpiration().after(now)
                || claims.getIssuedAt() == null || claims.getIssuedAt().after(now)
                || claims.get("email", String.class) == null
                || !(roles instanceof List<?> authorities) || authorities.isEmpty()
                || authorities.stream().anyMatch(role -> !(role instanceof String authority)
                    || !(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN")))) {
            throw new JwtException("Invalid access token claims");
        }
        return claims;
    }
}
