package com.taskflow.task_service.exception;

/**
 * Distinguishes failed project permission/reference lookup from a missing project.
 * FeignErrorDecoder and retryable-error handling construct it; advice returns 503 so callers can
 * retry instead of treating an outage as a permanent authorization/validation decision.
 */
public class ProjectServiceUnavailableException
        extends DownstreamServiceException {

    public ProjectServiceUnavailableException(
            String message
    ) {
        super("project-service", message);
    }

    public ProjectServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(
                "project-service",
                message,
                cause
        );
    }
}
