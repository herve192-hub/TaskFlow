package com.taskflow.task_service.dto.request;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Task description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Task priority is required")
    private TaskPriority priority;

    @NotNull(message = "Task type is required")
    private TaskType type;

    private String assigneeId;

    private String parentTaskId;

    private String sprintId;

    private Instant dueDate;

    private Double estimatedHours;

    private List<String> tags;
}