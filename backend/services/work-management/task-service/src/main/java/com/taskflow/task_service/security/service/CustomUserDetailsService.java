package com.taskflow.task_service.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

/**
 * Builds lightweight UserDetails from an already supplied account identifier without a database lookup.
 * It is not credential verification and uses an empty password plus ROLE_USER. The active request
 * filter validates JWTs independently; project/task permissions still require service-layer checks.
 */
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
