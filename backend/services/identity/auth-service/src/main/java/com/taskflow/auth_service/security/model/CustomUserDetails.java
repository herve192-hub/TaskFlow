package com.taskflow.auth_service.security.model;

import com.taskflow.auth_service.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapts credential-account fields to Spring Security's authenticated principal.
 * getUsername returns email for credential lookup; getId supplies the canonical JWT subject and
 * opaque audit actor. Platform roles become GrantedAuthority values. The current adapter's lock/
 * expiry methods return true; do not assume every domain.User status field is enforced here.
 */
@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String id;

    private String firstname;

    private String lastname;

    private String email;

    private String password;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()
                .map(role ->
                        new SimpleGrantedAuthority(role.name())
                )
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
