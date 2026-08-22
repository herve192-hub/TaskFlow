package com.taskflow.task_service.events;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;

import java.time.Instant;

public record TaskUpdatedEvent(

        String taskId,

        String projectId,

        String updatedBy,

        TaskStatus status,

        TaskPriority priority,

        TaskType type,

        Instant updatedAt

) {
}