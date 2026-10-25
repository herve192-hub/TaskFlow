package com.taskflow.task_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local projection of project-service's internal access-response contract.
 * role is a string to avoid sharing the project domain enum across service modules; permission
 * flags and canonical userId come from the owning service under the caller's forwarded token.
 * Keep this shape aligned with project-service DTOs when evolving the internal API.
 */
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
