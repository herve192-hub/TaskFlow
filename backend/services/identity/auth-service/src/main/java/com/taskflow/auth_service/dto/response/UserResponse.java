// 
package com.taskflow.auth.dto.response;

import com.taskflow.auth.domain.AuthProvider;
import com.taskflow.auth.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder

public class UserResponse {
    
    private String id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private Set<Role> roles;
    private AuthProvider provider;
    private boolean emailVerified;
    private String profileImage;
    private Instant createAt;

}
