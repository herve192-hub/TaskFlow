// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Reserved email input for account-recovery work, with basic Bean Validation constraints.
 * No recovery delivery or token issuance is implemented by the current AuthController.
 * Keep this distinction aligned with the frontend's administrator-recovery guidance.
 */
@Data

public class ForgotPasswordRequest {
    
    @Email
    @NotBlank
    private String email;

}
