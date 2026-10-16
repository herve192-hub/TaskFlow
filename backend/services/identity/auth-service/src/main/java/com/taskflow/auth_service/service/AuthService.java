package com.taskflow.auth_service.service;

import com.taskflow.auth_service.dto.request.LoginRequest;
import com.taskflow.auth_service.dto.request.RegisterRequest;
import com.taskflow.auth_service.dto.response.AuthResponse;

/** Credential-account registration and sign-in; browser cookie handling belongs to AuthController. */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
