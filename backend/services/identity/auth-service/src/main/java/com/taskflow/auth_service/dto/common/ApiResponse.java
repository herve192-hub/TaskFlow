// 
package com.taskflow.auth_service.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class ApiResponse {
    private boolean success;
    
    private String message;
}
