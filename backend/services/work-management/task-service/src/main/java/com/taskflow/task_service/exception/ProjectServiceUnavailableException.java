package com.taskflow.task_service.exception;

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