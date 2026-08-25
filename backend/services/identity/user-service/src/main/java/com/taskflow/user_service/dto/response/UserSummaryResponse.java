package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private String id;

    private String authUserId;

    private String fullName;

    private String email;

    private String avatarUrl;

    private UserStatus status;
}