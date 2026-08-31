package com.taskflow.task_service.exception;

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