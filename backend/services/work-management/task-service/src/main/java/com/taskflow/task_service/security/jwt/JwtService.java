package com.taskflow.task_service.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String extractUsername(String token);

    boolean isTokenValid(
            String token,
            UserDetails userDetails
    );

    boolean isTokenValid(String token);
}