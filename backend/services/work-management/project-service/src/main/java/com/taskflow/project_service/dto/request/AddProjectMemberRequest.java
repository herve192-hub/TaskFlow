package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Input for adding a member using canonical auth userId and a required project role.
 * ProjectServiceImpl checks the caller's management permission, target profile existence,
 * duplicate membership, and the prohibition on assigning OWNER through this generic route.
 */
@Data
public class AddProjectMemberRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}
