// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Legacy-spelled input model for a possible refresh-token exchange.
 * Current AuthService has no refresh operation; JWT access validation intentionally rejects
 * refresh-purpose tokens. Keep the existing class name for compatibility until a reviewed API change.
 */
@Data

public class RefreschTokenRequest {
    
    @NotBlank
    private String refreshToken;

}
