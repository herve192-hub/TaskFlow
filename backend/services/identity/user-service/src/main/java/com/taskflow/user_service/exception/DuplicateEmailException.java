package com.taskflow.user_service.exception;

/**
 * Signals a profile email collision during profile creation and maps to HTTP 409 in advice.
 * This is separate from auth-service registration uniqueness. The message contains the supplied
 * email, so avoid treating it as safe security-audit data or adding it to broad diagnostic logs.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }

}
