package com.taskflow.user_service.domain;

import com.taskflow.user_service.domain.enums.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Profile document in user-service's users collection, independent of auth's credential record.
 * Mongo id identifies this profile; authUserId is the canonical account link used in memberships
 * and assignments. Phone/bio/preferences are private profile fields, while directory DTOs expose
 * only their explicit summary fields. Auditing timestamps come from MongoConfig.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    /**
     * Canonical identity from auth-service.
     * This value matches the JWT subject ("sub").
     */
    @NotBlank
    @Indexed(unique = true)
    private String authUserId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    @Indexed(unique = true)
    private String email;

    private String phone;

    private String avatarUrl;

    private String bio;

    @Builder.Default
    private UserPreferences preferences = new UserPreferences();

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
