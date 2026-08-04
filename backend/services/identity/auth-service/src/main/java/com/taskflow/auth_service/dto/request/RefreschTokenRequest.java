// 
package com.taskflow.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class RefreschTokenRequest {
    
    @NotBlank
    private String refreshToken;

}
