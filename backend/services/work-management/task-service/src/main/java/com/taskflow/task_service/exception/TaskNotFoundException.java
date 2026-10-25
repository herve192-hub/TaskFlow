package com.taskflow.task_service.exception;

/**
 * Signals absent or soft-archived tasks on normal retrieval paths and maps to 404.
 * The unarchive flow deliberately uses a separate including-archived lookup with management
 * checks; changing archive visibility must keep these two paths distinct.
 */
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String message) {
        super(message);
    }
}
