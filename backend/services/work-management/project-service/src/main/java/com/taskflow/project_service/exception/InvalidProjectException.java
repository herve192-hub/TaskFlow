package com.taskflow.project_service.exception;

public class InvalidProjectException extends RuntimeException {

    public InvalidProjectException(String message) {
        super(message);
    }
}