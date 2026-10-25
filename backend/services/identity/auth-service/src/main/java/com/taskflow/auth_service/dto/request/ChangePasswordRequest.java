// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Reserved input shape containing old/new passwords for a future change-password operation.
 * Current AuthService/AuthController do not expose that operation. Any eventual endpoint must
 * verify the current account and password before persisting an encoded replacement; this DTO's
 * @NotBlank annotations alone do not implement password policy or authorization.
 */
@Data

public class ChangePasswordRequest {
    
    @NotBlank
    private String oldPassword;
    
    @NotBlank
    private String newPassword;

}
