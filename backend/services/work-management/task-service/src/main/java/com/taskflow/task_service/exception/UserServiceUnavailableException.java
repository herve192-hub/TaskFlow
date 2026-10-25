package com.taskflow.task_service.exception;

/**
 * Distinguishes failed profile existence checks from an invalid assignee.
 * Feign decoding and retryable failures map to this service-specific 503 error. Neither an
 * outage nor profile existence should be interpreted as project membership.
 */
public class UserServiceUnavailableException
        extends DownstreamServiceException {

    public UserServiceUnavailableException(
            String message
    ) {
        super("user-service", message);
    }

    public UserServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(
                "user-service",
                message,
                cause
        );
    }
}
