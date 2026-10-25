package com.taskflow.task_service.dto.response;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Reduced list/query representation for project and personal task pages.
 * Omits large descriptions/comments and full audit/effort data while retaining status, priority,
 * assignee, and due date needed by dashboard/list views. It is not a full TaskResponse.
 */
@Data
@Builder
public class TaskSummaryResponse {

    private String id;

    private String projectId;

    private String title;

    private TaskStatus status;

    private TaskPriority priority;

    private TaskType type;

    private String assigneeId;

    private Instant dueDate;

    private Instant createdAt;
}
