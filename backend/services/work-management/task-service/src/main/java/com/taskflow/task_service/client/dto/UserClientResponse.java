package com.taskflow.task_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Limited user-service internal profile projection using canonical authUserId.
 * Contains display metadata/status, not private phone/bio or credential data. It is a client
 * contract, not a task-owned user entity or proof of project membership.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientResponse {

    private String authUserId;

    private String firstName;

    private String lastName;

    private String email;

    private String avatarUrl;

    private String status;
}
