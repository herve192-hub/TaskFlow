package com.taskflow.project_service.exception;

public class UserServiceUnavailableException
        extends DownstreamServiceException {

    public UserServiceUnavailableException( String message ) {
        super( "user-service", message );
    }

    public UserServiceUnavailableException( String message, Throwable cause ) {
        super( "user-service", message, cause );
    }
}