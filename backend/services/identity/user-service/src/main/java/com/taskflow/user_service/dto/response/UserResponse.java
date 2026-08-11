package com.taskflow.user_service.dto.response;

import com.taskflow.user_service.domain.Address;
import com.taskflow.user_service.domain.Department;
import com.taskflow.user_service.domain.enums.Gender;
import com.taskflow.user_service.domain.enums.UserStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private String avatarUrl;

    private String jobTitle;

    private Department department;

    private Address address;

    private UserStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}