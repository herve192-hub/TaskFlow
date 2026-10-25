package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Current membership-derived permissions returned to the frontend and task-service.
 * Nonmembers receive false flags; role and canView/canEdit/canManageMembers come from project
 * membership. Task-service must request this under the caller's forwarded token rather than
 * accepting these flags from a browser payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAccessResponse {

    private String projectId;

    private String userId;

    private boolean member;

    private ProjectRole role;

    private boolean canView;

    private boolean canEdit;

    private boolean canManageMembers;
}
