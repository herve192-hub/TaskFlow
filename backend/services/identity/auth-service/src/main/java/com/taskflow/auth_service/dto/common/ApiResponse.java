// 
package com.taskflow.auth.dto.common;

import lombok.builder;
import lombok.Data;

@Data
@Builder

public class ApiResponse {
    private boolean success;
    
    private String message;
}
