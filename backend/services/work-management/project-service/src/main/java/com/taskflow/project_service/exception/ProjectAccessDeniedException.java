package com.taskflow.project_service.exception;

/**
 * Project-domain permission failure used by membership/ownership checks and Feign decoding.
 * Maps to HTTP 403 in advice; a correctly signed token establishes identity but does not
 * satisfy current project membership permissions by itself.
 */
public class ProjectAccessDeniedException extends RuntimeException {

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
