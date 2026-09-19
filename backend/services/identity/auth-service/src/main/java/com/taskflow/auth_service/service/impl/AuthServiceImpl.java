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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

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
                 * Your current User domain defaults enabled=false.
                 * Without verification logic implemented yet, that would
                 * prevent the newly registered user from logging in.
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

        savedUser.setRefreshToken(refreshToken);
        userRepository.save(savedUser);

        return buildAuthResponse(
                savedUser,
                accessToken,
                refreshToken
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

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

        user.setRefreshToken(refreshToken);
        user.setLastLoginAt(Instant.now());

        User savedUser
                = userRepository.save(user);

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
