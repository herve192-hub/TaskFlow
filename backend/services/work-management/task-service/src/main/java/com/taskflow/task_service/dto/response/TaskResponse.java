package com.taskflow.task_service.dto.response;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Detailed task projection returned after authorized create/read/update/lifecycle operations.
 * Contains logical project/account references and completion/archive metadata, not joined profiles
 * or a membership permission decision. Client read access still depends on the service endpoint.
 */
@Data
@Builder
public class TaskResponse {

    private String id;

    private String projectId;

    private String createdBy;

    private String assigneeId;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private TaskType type;

    private String parentTaskId;

    private String sprintId;

    private Instant dueDate;

    private Double estimatedHours;

    private Double actualHours;

    private List<String> tags;

    private boolean archived;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant completedAt;
}
