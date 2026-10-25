package com.taskflow.project_service.exception;

/**
 * Specific downstream-outage failure for profile lookups used in member validation.
 * Extends DownstreamServiceException so advice reports 503 rather than pretending a missing
 * profile or forbidden membership. Feign timeouts/retryable failures are translated here.
 */
public class UserServiceUnavailableException
        extends DownstreamServiceException {

    public UserServiceUnavailableException( String message ) {
        super( "user-service", message );
    }

    public UserServiceUnavailableException( String message, Throwable cause ) {
        super( "user-service", message, cause );
    }
}
