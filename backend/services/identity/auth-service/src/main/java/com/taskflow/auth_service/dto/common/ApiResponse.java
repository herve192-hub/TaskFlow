// 
package com.taskflow.auth_service.dto.common;

import lombok.Builder;
import lombok.Data;

/**
 * Small success/message response shape retained for auth API extensions.
 * The current controller returns AuthResponse for login/register and empty bodies for CSRF/logout;
 * this DTO is not evidence of another implemented endpoint and must never carry credentials.
 */
@Data
@Builder

public class ApiResponse {
    private boolean success;
    
    private String message;
}
