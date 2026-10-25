package com.taskflow.auth_service.security.service;

import com.taskflow.auth_service.domain.User;
import com.taskflow.auth_service.repository.UserRepository;
import com.taskflow.auth_service.security.model.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

/**
 * Loads a credential account by email for DaoAuthenticationProvider and JwtAuthenticationFilter.
 * Builds CustomUserDetails with the canonical auth ID, encoded verifier, roles, and enabled flag.
 * It reads auth's repository only; names/photos/preferences in the profile service are a separate flow.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email
                        )
                );

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
}
