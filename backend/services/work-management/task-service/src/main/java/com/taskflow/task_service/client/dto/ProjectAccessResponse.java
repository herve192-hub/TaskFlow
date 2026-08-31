package com.taskflow.task_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAccessResponse {

    private String projectId;

    private String userId;

    private boolean member;

    private String role;

    private boolean canView;

    private boolean canEdit;

    private boolean canManageMembers;
}