package com.taskflow.security;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Exercises fail-fast production origin/cookie/onboarding, RSA key distribution, MongoDB TLS, and Kafka rules.
 * Uses generated RSA material and synthetic example endpoints; no live provider is contacted.
 * These property-level checks complement deployment probes and do not verify external IAM/backups.
 */
class ProductionSecurityTest {
    private static KeyPair keys;
    private static String publicKey;
    private static String privateKey;

    private static String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n" + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }

    @BeforeAll
    static void generateKeys() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        keys = generator.generateKeyPair();
        publicKey = pem("PUBLIC KEY", keys.getPublic().getEncoded());
        privateKey = pem("PRIVATE KEY", keys.getPrivate().getEncoded());
    }

    private static MockEnvironment production() {
        return new MockEnvironment().withProperty("TASKFLOW_WEB_ORIGIN", "https://taskflow.example.test")
                .withProperty("TASKFLOW_COOKIE_SECURE", "true")
                .withProperty("TASKFLOW_PUBLIC_REGISTRATION_ENABLED", "false")
                .withProperty("spring.application.name", "user-service")
                .withProperty("jwt.public-key", publicKey)
                .withProperty("spring.mongodb.uri", "mongodb://app:credential@mongo.example.test/taskflow-users?tls=true");
    }

    private static void check(MockEnvironment environment) throws Exception {
        new ProductionSecurity().productionSecurityChecks(environment).afterPropertiesSet();
    }

    @Test
    void acceptsSeparatedKeysAndVerifiedTransport() throws Exception {
        check(production());
        check(production().withProperty("spring.application.name", "auth-service").withProperty("jwt.private-key", privateKey));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "mongodb://app:credential@mongo.example.test/db", "mongodb://app:tls=true@mongo.example.test/db",
            "mongodb+srv://app:credential@mongo.example.test/db?tls=false",
            "mongodb://app:credential@mongo.example.test/db?tls=true&tlsInsecure=true",
            "mongodb://app:credential@mongo.example.test/db?tls=true&tlsAllowInvalidHostnames=true",
            "mongodb://app:credential@mongo.example.test/db?tls=true&tlsAllowInvalidCertificates=true",
            "mongodb://app:credential@mongo.example.test/db?tls=true&tls=false"
    })
    void rejectsPlaintextAndDisabledCertificateVerification(String uri) {
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("spring.mongodb.uri", uri)));
    }

    @Test
    void rejectsUnsafeBrowserAndSigningConfiguration() {
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("TASKFLOW_COOKIE_SECURE", "false")));
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("TASKFLOW_PUBLIC_REGISTRATION_ENABLED", "true")));
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("TASKFLOW_WEB_ORIGIN", "http://taskflow.example.test")));
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("jwt.private-key", privateKey)));
        assertThrows(IllegalArgumentException.class, () -> check(production().withProperty("jwt.public-key", "invalid-key")));
        assertThrows(IllegalStateException.class, () -> check(production().withProperty("spring.application.name", "auth-service")
                .withProperty("jwt.private-key", privateKey).withProperty("jwt.expiration", "3600000")));
    }

    @Test
    void rejectsKafkaWithoutAuthenticationAndHostnameVerification() {
        var environment = production().withProperty("spring.application.name", "task-service");
        assertThrows(IllegalStateException.class, () -> check(environment));
        environment.withProperty("spring.kafka.properties.security.protocol", "SASL_SSL");
        assertThrows(IllegalStateException.class, () -> check(environment));
        environment.withProperty("spring.kafka.properties.sasl.mechanism", "SCRAM-SHA-512")
                .withProperty("spring.kafka.properties.sasl.jaas.config", "test-only-placeholder");
        assertDoesNotThrow(() -> check(environment));
        environment.withProperty("spring.kafka.properties.ssl.endpoint.identification.algorithm", "");
        assertThrows(IllegalStateException.class, () -> check(environment));
    }

    @Test
    void verifiesRsaAccessTokensAndRejectsPublicKeyAlgorithmConfusion() {
        var builder = Jwts.builder().subject("user-1").issuer("taskflow").audience().add("taskflow-api").and()
                .id("session-1").issuedAt(Date.from(Instant.now().minusSeconds(5)))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .claim("email", "user@example.test").claim("roles", List.of("ROLE_USER")).claim("token_use", "access");
        String token = builder.signWith(JwtKeys.privateKey(privateKey), Jwts.SIG.RS256).compact();
        assertEquals("user-1", AccessTokenValidator.parse(token, null, "taskflow", publicKey).getSubject());
        String confused = builder.signWith(Keys.hmacShaKeyFor(publicKey.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS512).compact();
        assertThrows(RuntimeException.class, () -> AccessTokenValidator.parse(confused, null, "taskflow", publicKey));
    }
}
