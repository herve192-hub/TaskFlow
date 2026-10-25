package com.taskflow.task_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Nonblank canonical assignee account ID for the dedicated assignment endpoint.
 * TaskServiceImpl requires project-management rights, profile existence, and project membership.
 * This request does not accept a profile document ID or silently grant a project role.
 */
@Data
public class AssignTaskRequest {

    @NotBlank(message = "Assignee ID is required")
    private String assigneeId;
}
