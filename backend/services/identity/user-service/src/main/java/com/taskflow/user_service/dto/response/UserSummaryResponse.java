package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private String id;

    private String fullName;

    private String email;

    private String avatarUrl;

    private String department;

    private UserStatus status;
}