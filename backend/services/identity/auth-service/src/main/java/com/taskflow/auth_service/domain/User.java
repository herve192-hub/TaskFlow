// 
package com.taskflow.auth_service.domain;


import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.event.AuditingEntityCallback;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Configuration;

@Document(collection = "users")

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String password;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean emailVerified = false;

    private String profileImage;

    private String phoneNumber;

    private String refreshToken;

    private Instant lastLoginAt;

    // private Instant createdAt;

    // private Instant updatedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant UpdateAt;

    @Configuration
    @EnableMongoAuditing

    public class MongoConfig {

    }

}