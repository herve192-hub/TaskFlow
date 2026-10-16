package com.taskflow.security;

import java.net.URI;
import java.util.Locale;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Startup checks imported by the gateway and business services under the prod profile.
 * Validate configured transport and authentication requirements before serving requests;
 * these checks do not establish database privileges or verify external infrastructure.
 */
@Configuration
@Profile("prod")
public class ProductionSecurity {
    @Bean
    InitializingBean productionSecurityChecks(Environment environment) {
        return () -> {
            URI origin = URI.create(environment.getRequiredProperty("TASKFLOW_WEB_ORIGIN"));
            if (!"https".equals(origin.getScheme()) || origin.getHost() == null
                    || origin.getUserInfo() != null || origin.getQuery() != null || origin.getFragment() != null
                    || (origin.getPath() != null && !origin.getPath().isEmpty())) {
                throw new IllegalStateException("Production requires one explicit HTTPS browser origin");
            }
            if (!"true".equalsIgnoreCase(environment.getProperty("TASKFLOW_COOKIE_SECURE", "false"))) {
                throw new IllegalStateException("Production requires Secure authentication and CSRF cookies");
            }
            if (environment.getProperty("TASKFLOW_PUBLIC_REGISTRATION_ENABLED", Boolean.class, true)) {
                throw new IllegalStateException("Production registration requires administrator approval");
            }
            // The gateway routes requests; business services own JWT and database validation.
            if (!"api-gateway".equals(environment.getProperty("spring.application.name"))) {
                var publicKey = JwtKeys.publicKey(environment.getRequiredProperty("jwt.public-key"));
                if ("auth-service".equals(environment.getProperty("spring.application.name"))) {
                    var privateKey = JwtKeys.privateKey(environment.getRequiredProperty("jwt.private-key"));
                    if (!publicKey.getModulus().equals(privateKey.getModulus())) {
                        throw new IllegalStateException("JWT signing and verification keys must match");
                    }
                    long lifetime = environment.getProperty("jwt.expiration", Long.class, 900_000L);
                    if (lifetime < 60_000 || lifetime > 900_000) {
                        throw new IllegalStateException("Production access token lifetime must be 1–15 minutes");
                    }
                } else if (environment.containsProperty("jwt.private-key")) {
                    throw new IllegalStateException("Only the auth service may receive the JWT private key");
                }
                URI mongo;
                try {
                    mongo = URI.create(environment.getRequiredProperty("spring.mongodb.uri"));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalStateException("Invalid production MongoDB connection URI");
                }
                // Decode and normalize URI options so alternate casing cannot bypass TLS checks.
                Map<String, String> options = new HashMap<>();
                for (String option : (mongo.getRawQuery() == null ? "" : mongo.getRawQuery()).split("&")) {
                    if (option.isEmpty()) continue;
                    String[] pair = option.split("=", 2);
                    String name = URLDecoder.decode(pair[0], StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
                    String value = pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8).toLowerCase(Locale.ROOT) : "";
                    if (options.put(name, value) != null) throw new IllegalStateException("Duplicate MongoDB connection options are not permitted");
                }
                boolean secure = "mongodb+srv".equals(mongo.getScheme())
                        || ("mongodb".equals(mongo.getScheme()) && ("true".equals(options.get("tls")) || "true".equals(options.get("ssl"))));
                if (!secure || "false".equals(options.get("tls")) || "false".equals(options.get("ssl"))
                        || "true".equals(options.get("tlsinsecure")) || "true".equals(options.get("tlsallowinvalidcertificates"))
                        || "true".equals(options.get("tlsallowinvalidhostnames"))) {
                    throw new IllegalStateException("Production MongoDB must use verified TLS");
                }
                // Task-service publishes Kafka events; other business services do not need broker secrets.
                if ("task-service".equals(environment.getProperty("spring.application.name"))
                        && (!"https".equalsIgnoreCase(environment.getProperty("spring.kafka.properties.ssl.endpoint.identification.algorithm", "https"))
                            || environment.getProperty("spring.kafka.properties.sasl.mechanism", "").isBlank()
                            || environment.getProperty("spring.kafka.properties.sasl.jaas.config", "").isBlank())) {
                    throw new IllegalStateException("Production Kafka requires authentication and hostname verification");
                }
                if ("task-service".equals(environment.getProperty("spring.application.name"))
                        && !"SASL_SSL".equals(environment.getProperty("spring.kafka.properties.security.protocol"))) {
                    throw new IllegalStateException("Production Kafka must use SASL_SSL");
                }
            }
        };
    }
}
