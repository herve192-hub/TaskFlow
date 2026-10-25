package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * Membership-list projection: id is a membership document ID and userId is a canonical account ID.
 * The browser resolves names/photos through user-service lookup; no profile join occurs in this
 * DTO. Use id for removal/role-change routes and userId for assignments/directory requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    private String id;

    private String userId;

    private ProjectRole role;

    private Instant joinedAt;
}
