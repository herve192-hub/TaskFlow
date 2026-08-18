
package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProjectMemberRequest {

    @NotNull(message = "Project role is required")
    private ProjectRole role;
}