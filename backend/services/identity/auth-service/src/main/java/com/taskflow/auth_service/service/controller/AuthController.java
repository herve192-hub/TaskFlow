package com.taskflow.auth_service.controller;

import com.taskflow.auth_service.dto.request.LoginRequest;
import com.taskflow.auth_service.dto.request.RegisterRequest;
import com.taskflow.auth_service.dto.response.AuthResponse;
import com.taskflow.auth_service.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import com.taskflow.auth_service.security.jwt.JwtProperties;
import com.taskflow.security.CookieSecurity;
import java.time.Duration;

/** Authentication API returning client tokens and setting the browser's HttpOnly access cookie. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @Value("${TASKFLOW_COOKIE_SECURE:false}")
    private boolean secureCookie;

    @Value("${TASKFLOW_PUBLIC_REGISTRATION_ENABLED:true}")
    private boolean publicRegistration;

    /** Materialize the deferred CSRF token so the browser receives its nonce cookie. */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(HttpServletRequest request) {
        ((CsrfToken) request.getAttribute(CsrfToken.class.getName())).getToken();
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    /** Clear this browser's cookie; previously issued bearer tokens expire independently. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        String actor = authentication.getPrincipal() instanceof
                com.taskflow.auth_service.security.model.CustomUserDetails user ? user.getId() : null;
        com.taskflow.security.SecurityAudit.record("sign_out", actor, "auth");
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, accessCookie("", Duration.ZERO)).build();
    }

    private String accessCookie(String token, Duration lifetime) {
        return ResponseCookie.from(CookieSecurity.accessCookieName(secureCookie), token)
                .httpOnly(true).secure(secureCookie).sameSite("Strict").path("/")
                .maxAge(lifetime).build().toString();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication
    ) {

        // Registration is reachable anonymously for development; production requires a platform admin.
        if (!publicRegistration && (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())))) {
            throw new org.springframework.security.access.AccessDeniedException("Administrator approval is required");
        }
        AuthResponse response
                = authService.register(request);

        var result = ResponseEntity
                .status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore());
        // Approved account provisioning must preserve the administrator's existing browser identity.
        if (publicRegistration) {
            result.header(HttpHeaders.SET_COOKIE,
                    accessCookie(response.getAccessToken(), Duration.ofMillis(jwtProperties.getExpiration())));
        }
        return result.body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, accessCookie(response.getAccessToken(), Duration.ofMillis(jwtProperties.getExpiration())))
                .body(response);
    }
}
