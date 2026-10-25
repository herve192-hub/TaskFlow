// 
package com.taskflow.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Authentication result used internally between AuthService and AuthController and returned to API clients.
 * Contains access/refresh credentials plus safe account metadata. The controller also writes the
 * HttpOnly access cookie and marks responses no-store. Browser authService keeps display metadata
 * only and deliberately does not persist these token fields in localStorage/sessionStorage.
 */
@Data
@Builder

public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponse user;

}
