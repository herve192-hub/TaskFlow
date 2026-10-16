package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Directory/team display data, including email, without the full profile's phone or bio fields. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private String id;

    private String authUserId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String avatarUrl;

    private UserStatus status;
}
