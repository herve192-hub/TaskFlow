// 
package com.taskflow.auth_service.dto.response;

import com.taskflow.auth_service.domain.AuthProvider;
import com.taskflow.auth_service.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

/**
 * Safe credential-account metadata included in AuthResponse, distinct from the profile service response.
 * Its ID is the JWT subject used across services. AuthServiceImpl maps names/roles/provider state
 * explicitly, excluding the encoded password and refresh-token digest from serialization.
 */
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
    private Instant createdAt;

}
