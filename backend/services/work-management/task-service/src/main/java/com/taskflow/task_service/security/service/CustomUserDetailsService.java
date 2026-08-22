package com.taskflow.task_service.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        /*
         * Task-service does not own user authentication.
         *
         * The JWT already contains the authenticated user's
         * identity. We construct a lightweight UserDetails
         * object from the JWT subject.
         */

        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .authorities("ROLE_USER")
                .build();
    }
}