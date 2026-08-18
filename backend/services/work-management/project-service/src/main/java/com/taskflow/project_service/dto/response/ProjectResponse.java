package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProjectResponse {

    private String id;

    private String name;

    private String description;

    private ProjectStatus status;

    private ProjectVisibility visibility;

    private String ownerId;

    private List<ProjectMemberResponse> members;

    private Instant createdAt;

    private Instant updatedAt;
}