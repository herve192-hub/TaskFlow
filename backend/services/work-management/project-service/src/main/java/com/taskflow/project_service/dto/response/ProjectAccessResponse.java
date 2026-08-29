package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectRole;

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

    private ProjectRole role;

    private boolean canView;

    private boolean canEdit;

    private boolean canManageMembers;
}