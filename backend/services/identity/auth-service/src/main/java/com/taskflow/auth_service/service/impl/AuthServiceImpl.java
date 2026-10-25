package com.taskflow.auth_service.service.impl;

import com.taskflow.auth_service.domain.AuthProvider;
import com.taskflow.auth_service.domain.Role;
import com.taskflow.auth_service.domain.User;
import com.taskflow.auth_service.dto.request.LoginRequest;
import com.taskflow.auth_service.dto.request.RegisterRequest;
import com.taskflow.auth_service.dto.response.AuthResponse;
import com.taskflow.auth_service.dto.response.UserResponse;
import com.taskflow.auth_service.repository.UserRepository;
import com.taskflow.auth_service.security.jwt.JwtService;
import com.taskflow.auth_service.security.model.CustomUserDetails;
import com.taskflow.auth_service.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.Locale;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Coordinates normalized account registration, credential verification, token issuance, and audit events.
 * UserRepository owns credential records, PasswordEncoder hashes/migrates verifiers, and JwtService
 * creates purpose-specific tokens. Only refresh-token digests are persisted; response credentials are
 * transient. Registration assigns ROLE_USER, while AuthController enforces production admin approval.
 * No distributed transaction creates a user-service profile; the browser initializes that separately.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    // Canonicalize identity before both uniqueness checks and persistence. This creates
    // only the credential account; user-service profile initialization is a separate request.
    public AuthResponse register(RegisterRequest request) {
        // Use the same canonical email for uniqueness checks and subsequent sign-in lookups.
        request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        request.setUsername(request.getUsername().trim());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "An account already exists with this email"
            );
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username is already in use"
            );
        }

        User user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .provider(AuthProvider.LOCAL)
                /*
                 * Email verification is not implemented; explicitly enable approved accounts
                 * while retaining emailVerified=false. Production approval is enforced by the controller.
                 */
                .enabled(true)
                .emailVerified(false)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails
                = toUserDetails(savedUser);

        String accessToken
                = jwtService.generateAccessToken(userDetails);

        String refreshToken
                = jwtService.generateRefreshToken(userDetails);

        savedUser.setRefreshToken(tokenDigest(refreshToken));
        userRepository.save(savedUser);
        com.taskflow.security.SecurityAudit.record("account_registered", savedUser.getId(), savedUser.getId());

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken
        );
    }

    @Override
    @Transactional
    // Authenticate through the configured provider before issuing any token. Persisting a
    // new refresh digest does not revoke already issued access JWTs in other services.
    public AuthResponse login(LoginRequest request) {
        request.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));

        Authentication authentication
                = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails
                = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "User not found"
                )
                );

        String accessToken
                = jwtService.generateAccessToken(userDetails);

        String refreshToken
                = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(tokenDigest(refreshToken));
        // A successful login proves the plaintext password before a legacy hash is upgraded.
        if (passwordEncoder.upgradeEncoding(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setLastLoginAt(Instant.now());

        User savedUser
                = userRepository.save(user);
        com.taskflow.security.SecurityAudit.record("sign_in", savedUser.getId(), savedUser.getId());

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken
        );
    }

    private CustomUserDetails toUserDetails(User user) {

        return CustomUserDetails.builder()
                .id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .build();
    }

    /** Persist only the refresh-token digest; the raw credential belongs only in the response. */
    private String tokenDigest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private AuthResponse buildAuthResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .provider(user.getProvider())
                .emailVerified(user.isEmailVerified())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
