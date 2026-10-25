package com.taskflow.project_service.exception;

/**
 * Signals a case-insensitive name collision within the same owner's projects.
 * The service performs the check and GlobalExceptionHandler maps it to HTTP 409.
 * Names belonging to different owners are not treated as one global uniqueness namespace.
 */
public class DuplicateProjectException extends RuntimeException {

    public DuplicateProjectException(String message) {
        super(message);
    }
}
