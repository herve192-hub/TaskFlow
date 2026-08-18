package com.taskflow.project_service.dto.request;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private ProjectStatus status;

    private ProjectVisibility visibility;
}