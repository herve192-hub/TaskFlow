package com.taskflow.project_service.exception;

/**
 * Base failure type carrying the name of an unavailable collaborating service.
 * GlobalExceptionHandler maps it to a bounded 503 response; retaining the cause supports local
 * diagnostics without serializing an upstream response or caller token.
 */
public class DownstreamServiceException extends RuntimeException {

    private final String serviceName;

    public DownstreamServiceException( String serviceName, String message ) {
        super(message);
        this.serviceName = serviceName;
    }

    public DownstreamServiceException( String serviceName, String message, Throwable cause ) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
