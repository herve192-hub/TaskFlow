package com.taskflow.user_service.exception;

/**
 * Carries application-owned validation failures such as duplicate profiles or invalid preferences.
 * GlobalExceptionHandler maps it to a 400 ProblemDetail; use bounded messages without avatar
 * payloads, credentials, or other private input.
 */
public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String message) {
        super(message);
    }

}
