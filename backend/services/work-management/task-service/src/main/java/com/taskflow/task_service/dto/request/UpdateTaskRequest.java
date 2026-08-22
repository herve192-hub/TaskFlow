package com.taskflow.task_service.dto.request;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UpdateTaskRequest {

    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Task description cannot exceed 5000 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private TaskType type;

    private String assigneeId;

    private String sprintId;

    private Instant dueDate;

    private Double estimatedHours;

    private Double actualHours;

    private List<String> tags;
}