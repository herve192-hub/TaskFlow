package com.taskflow.task_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String avatarUrl;

    private String status;
}