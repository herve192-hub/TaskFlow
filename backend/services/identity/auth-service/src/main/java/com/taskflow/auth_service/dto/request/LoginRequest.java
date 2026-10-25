// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Credential payload bound and validated by POST /auth/login.
 * AuthServiceImpl canonicalizes email before AuthenticationManager checks the supplied password.
 * It contains plaintext input transiently, so controllers, exceptions, and audit logs must not echo it.
 */
@Data

public class LoginRequest {
    
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;    

}
