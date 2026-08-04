package com.taskflow.auth_service.security.service;

import com.taskflow.auth_service.domain.Role;
import com.taskflow.auth_service.domain.User;
import com.taskflow.auth_service.repository.UserRepository;
import com.taskflow.auth_service.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + email));

        Role role = user.getRoles().stream().findFirst().orElse(null);

        return CustomUserDetails.builder()
                .id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(role)
                .enabled(user.isEnabled())
                .build();
    }
}
