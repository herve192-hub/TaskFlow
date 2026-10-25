package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.enums.UserStatus;
import com.taskflow.user_service.domain.UserPreferences;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Private/self-service profile representation produced by UserMapper.
 * Contains both profile id and canonical authUserId plus contact/profile/preferences fields.
 * UserController restricts full-profile reads; directory callers use UserSummaryResponse instead.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String authUserId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phone;

    private String avatarUrl;

    private String bio;

    private UserPreferences preferences;

    private UserStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
