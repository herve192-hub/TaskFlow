package com.taskflow.task_service.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Verification/claim-access boundary used by task-service request authentication.
 * The caller identity is the canonical token subject (despite the method name extractUsername), not a profile document ID or requested path user.
 * Implementations delegate the security contract to common-security; this service does not issue
 * credential-account tokens or implement refresh exchange.
 */
public interface JwtService {

    String extractUsername(String token);

    boolean isTokenValid(
            String token,
            UserDetails userDetails
    );

    boolean isTokenValid(String token);
}
