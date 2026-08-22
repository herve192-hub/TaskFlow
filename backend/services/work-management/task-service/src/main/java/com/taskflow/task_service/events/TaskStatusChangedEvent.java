package com.taskflow.task_service.events;

import com.taskflow.task_service.domain.enums.TaskStatus;

import java.time.Instant;

public record TaskStatusChangedEvent(

        String taskId,

        String projectId,

        TaskStatus previousStatus,

        TaskStatus newStatus,

        String changedBy,

        Instant changedAt

) {
}