package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Optional project metadata changes; null fields retain their stored values in ProjectMapper.
 * Owner identity and membership are not writable here. ProjectServiceImpl requires project
 * edit rights and checks renamed projects against the owner's existing names.
 */
@Data
public class UpdateProjectRequest {

    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private ProjectStatus status;

    private ProjectVisibility visibility;
}
