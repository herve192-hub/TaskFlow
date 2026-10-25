package com.taskflow.task_service.exception;

/**
 * Signals a case-insensitive duplicate active task title within a project during creation.
 * GlobalExceptionHandler maps it to 409. Archived tasks are excluded from that repository check;
 * this exception is not a database-wide uniqueness guarantee across concurrent operations.
 */
public class DuplicateTaskException extends RuntimeException {

    public DuplicateTaskException(String message) {
        super(message);
    }
}
