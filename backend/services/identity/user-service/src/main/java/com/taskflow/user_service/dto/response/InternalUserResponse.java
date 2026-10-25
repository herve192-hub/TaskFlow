package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profile data returned only by authenticated internal lookup to collaborating services.
 * Uses canonical authUserId plus names/email/avatar/status for reference validation and display.
 * It omits private phone/bio/preferences and all authentication credentials.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponse {

    private String authUserId;

    private String firstName;

    private String lastName;

    private String email;

    private String avatarUrl;

    private UserStatus status;
}
