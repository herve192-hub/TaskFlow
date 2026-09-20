package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryResponse {

    private String id;

    private String name;

    private String description;

    private ProjectStatus status;

    private ProjectVisibility visibility;

    private String ownerId;

    private long memberCount;

    private Instant createdAt;

    private Instant updatedAt;
}
