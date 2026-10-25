package com.taskflow.task_service.dto.request;

import com.taskflow.task_service.domain.enums.TaskStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Required target status for the dedicated lifecycle endpoint.
 * The client cannot set completedAt or publish an event directly. TaskLifecyclePolicy checks the
 * state edge and TaskServiceImpl checks actor permissions, persists state, and emits domain events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTaskStatusRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;
}
