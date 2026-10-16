package com.taskflow.task_service.domain;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Task document with soft deletion; account references use auth-service IDs rather than profile IDs. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    /**
     * Project this task belongs to.
     */
    private String projectId;

    /**
     * User who created the task.
     */
    private String createdBy;

    /**
     * User currently assigned to the task.
     */
    private String assigneeId;

    private String title;

    private String description;

    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Builder.Default
    private TaskType type = TaskType.TASK;

    /**
     * Optional parent task for subtasks.
     */
    private String parentTaskId;

    /**
     * Optional sprint identifier.
     */
    private String sprintId;

    /**
     * Optional due date.
     */
    private Instant dueDate;

    /**
     * Estimated effort in hours.
     */
    private Double estimatedHours;

    /**
     * Actual time spent in hours.
     */
    private Double actualHours;

    /**
     * Tags associated with the task.
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * IDs of users watching the task.
     */
    @Builder.Default
    private List<String> watcherIds = new ArrayList<>();

    /**
     * Soft-delete flag.
     */
    @Builder.Default
    private boolean archived = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /** Set when entering COMPLETED and cleared when completed work is reopened. */
    private Instant completedAt;
}
