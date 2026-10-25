// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Validated input to account creation, shared by public development and admin-approved production flows.
 * Names, username, email, and passphrase have bounded lengths; AuthController decides whether the
 * caller may register an account. Roles, canonical ID, password encoding, and approval state are
 * assigned server-side rather than accepted as writable fields.
 */
@Data

public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstname;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastname;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20)
    private String username;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 15, max = 128)
    private String password;

}
