package com.taskflow.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Reserved password-change form model; current UserController exposes no password operation.
 * Passwords are owned by auth-service, not profile documents. Matching new/confirmation fields
 * and verifying the old credential would require additional application logic before use.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
