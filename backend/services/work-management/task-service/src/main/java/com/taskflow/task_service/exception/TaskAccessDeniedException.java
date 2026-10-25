package com.taskflow.task_service.exception;

/**
 * Permission failure for current project membership, management, or task actor restrictions.
 * GlobalExceptionHandler maps it to 403. A valid JWT proves identity; it does not authorize a
 * lifecycle edge, assignment change, or mutation of another member's task by itself.
 */
public class TaskAccessDeniedException extends RuntimeException {

    public TaskAccessDeniedException(String message) {
        super(message);
    }
}
