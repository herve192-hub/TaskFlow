package com.taskflow.project_service.exception;

public class ProjectAccessDeniedException extends RuntimeException {

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}