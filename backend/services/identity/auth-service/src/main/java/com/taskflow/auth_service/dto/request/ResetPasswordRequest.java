// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Reserved recovery-token/new-password input, not a currently exposed reset API.
 * Its existing minimum-length annotation is not the production registration password contract.
 * Implementing recovery would also require token verification, expiry, identity checks, and a
 * reviewed password policy; retaining this DTO does not provide those behaviors.
 */
@Data
public class ResetPasswordRequest {
    
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

}
