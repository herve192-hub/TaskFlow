package com.taskflow.project_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProjectNotFound(
            ProjectNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateProjectException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProject(
            DuplicateProjectException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_PROJECT",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            ProjectAccessDeniedException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "PROJECT_ACCESS_DENIED",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidProjectException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidProject(
            InvalidProjectException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PROJECT",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Request validation failed");
        response.put("path", request.getRequestURI());
        response.put("validationErrors", validationErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
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
            String errorCode,
            String message,
            HttpServletRequest request
    ) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", status.value());
        response.put("error", errorCode);
        response.put("message", message);
        response.put("path", request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(response);
    }
}