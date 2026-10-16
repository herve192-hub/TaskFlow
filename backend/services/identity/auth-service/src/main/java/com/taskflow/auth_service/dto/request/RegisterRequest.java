// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Account input with bounded names and 15-128 character passphrases; roles are assigned server-side. */
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
