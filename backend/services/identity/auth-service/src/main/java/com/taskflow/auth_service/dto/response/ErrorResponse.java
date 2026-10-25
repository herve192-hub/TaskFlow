package com.taskflow.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Legacy structured error envelope with timestamp/status/message/details fields.
 * AuthExceptionHandler currently returns bounded message maps instead; this model does not decide
 * status codes or redact sensitive input automatically. Do not populate details with credentials.
 */
@Data
@Builder

public class ErrorResponse {
    
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> details;
    
}
