package com.taskflow.task_service.exception;

/**
 * Domain-level invalid input/state-transition/reference failure, mapped to HTTP 400.
 * Service-side checks also protect direct callers outside MVC; DTO validation remains a separate
 * HTTP boundary. Messages should not contain private request bodies or credential material.
 */
public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}
