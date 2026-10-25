package com.taskflow.auth_service.service;

import com.taskflow.auth_service.dto.request.LoginRequest;
import com.taskflow.auth_service.dto.request.RegisterRequest;
import com.taskflow.auth_service.dto.response.AuthResponse;

/**
 * Application boundary implemented by AuthServiceImpl for registration and login.
 * The controller handles cookie/CSRF transport and production provisioning approval; this service
 * handles credential persistence and token issuance. Recovery and refresh exchange are not declared.
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
