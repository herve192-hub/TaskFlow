package com.taskflow.user_service.exception;

/**
 * Represents a missing profile lookup by document/account/email identifier.
 * GlobalExceptionHandler returns 404; frontend profile initialization distinguishes a missing
 * profile from authentication failure. A missing profile does not imply no auth account exists.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }

}
