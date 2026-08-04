package com.taskflow.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder

public class ErrorResponse {
    
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private List<String> details;
    
}
