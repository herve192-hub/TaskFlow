package com.taskflow.task_service.exception;

public class DuplicateTaskException extends RuntimeException {

    public DuplicateTaskException(String message) {
        super(message);
    }
}