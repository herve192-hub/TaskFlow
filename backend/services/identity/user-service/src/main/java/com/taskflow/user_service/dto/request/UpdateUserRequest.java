package com.taskflow.user_service.dto.request;

import com.taskflow.user_service.domain.Address;
import com.taskflow.user_service.domain.enums.Gender;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    private Gender gender;

    private String phoneNumber;

    private String jobTitle;

    private Address address;

    private String departmentId;
}