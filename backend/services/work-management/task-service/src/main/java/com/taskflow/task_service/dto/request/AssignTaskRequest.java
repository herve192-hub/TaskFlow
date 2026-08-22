package com.taskflow.task_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignTaskRequest {

    @NotBlank(message = "Assignee ID is required")
    private String assigneeId;
}