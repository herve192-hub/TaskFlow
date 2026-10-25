package com.taskflow.project_service.exception;

/**
 * Represents invalid project/member input or an invalid referenced profile.
 * Service methods also throw it when called outside MVC, supplementing DTO validation.
 * The global advice exposes its bounded message as HTTP 400.
 */
public class InvalidProjectException extends RuntimeException {

    public InvalidProjectException(String message) {
        super(message);
    }
}
