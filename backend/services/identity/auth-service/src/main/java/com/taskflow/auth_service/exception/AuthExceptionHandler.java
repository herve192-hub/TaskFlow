package com.taskflow.auth_service.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translate account failures into bounded responses without echoing passwords or rejected values. */
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Administrator approval is required"));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> authenticationFailure() {
        com.taskflow.security.SecurityAudit.record("sign_in_denied", null, "auth");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validationFailure() {
        // Never reflect passwords or rejected values, or let the framework log them.
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid account details"));
    }

    @ExceptionHandler({IllegalArgumentException.class, org.springframework.dao.DuplicateKeyException.class})
    public ResponseEntity<Map<String, String>> registrationFailure() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Unable to register with those account details"));
    }
}
