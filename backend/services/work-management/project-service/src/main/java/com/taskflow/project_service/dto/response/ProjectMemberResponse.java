package com.taskflow.project_service.dto.response;

import com.taskflow.project_service.domain.enums.ProjectRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProjectMemberResponse {

    private String id;

    private String userId;

    private ProjectRole role;

    private Instant joinedAt;
}