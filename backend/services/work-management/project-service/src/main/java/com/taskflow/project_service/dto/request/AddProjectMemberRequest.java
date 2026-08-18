package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddProjectMemberRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}