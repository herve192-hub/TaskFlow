package com.taskflow.task_service.dto.request;

import com.taskflow.task_service.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeTaskStatusRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}