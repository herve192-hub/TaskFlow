package com.taskflow.task_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTaskNotFound(
            TaskNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "TASK_NOT_FOUND",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateTaskException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateTask(
            DuplicateTaskException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_TASK",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            TaskAccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "TASK_ACCESS_DENIED",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidTaskException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTask(
            InvalidTaskException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_TASK",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(body);
    }


    @ExceptionHandler(
        DownstreamServiceException.class
        )
                public ResponseEntity<Map<String, Object>>
                handleDownstreamServiceException(
                        DownstreamServiceException exception,
                        HttpServletRequest request
                ) {

                return buildResponse(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "DOWNSTREAM_SERVICE_UNAVAILABLE",
                        exception.getMessage(),
                        request
                );
        }
}