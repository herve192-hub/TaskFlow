package com.taskflow.project_service.exception;

public class DuplicateProjectException extends RuntimeException {

    public DuplicateProjectException(String message) {
        super(message);
    }
}