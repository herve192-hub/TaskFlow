package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Detailed project metadata returned after authorized CRUD operations.
 * The members field exists in this schema, but ProjectMapper does not fetch memberships to
 * hydrate it; the paginated members endpoint is the active team source. ownerId is an auth ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
