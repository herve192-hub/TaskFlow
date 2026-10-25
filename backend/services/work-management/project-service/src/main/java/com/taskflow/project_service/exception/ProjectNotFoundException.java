package com.taskflow.project_service.exception;

/**
 * Represents a missing project during a service lookup and maps to HTTP 404.
 * It is distinct from a caller who lacks membership in an existing project; both outcomes
 * are chosen by ProjectServiceImpl rather than by the repository interface.
 */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String message) {
        super(message);
    }
}
