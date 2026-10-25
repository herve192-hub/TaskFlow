package com.taskflow.task_service.exception;

/**
 * Base dependency-outage error carrying the collaborating service name and optional cause.
 * GlobalExceptionHandler maps it to a bounded 503 response instead of pretending access was denied
 * or an entity was absent. Do not include forwarded credentials or full response bodies in messages.
 */
public class DownstreamServiceException extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException(
            String serviceName,
            String message
    ) {
        super(message);
        this.serviceName = serviceName;
    }

    public DownstreamServiceException(
            String serviceName,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
