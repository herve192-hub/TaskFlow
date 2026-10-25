package com.taskflow.task_service.events;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;

import java.time.Instant;

/**
 * Declared generic-update event shape, not emitted by the current updateTask implementation.
 * The active externally published events are task status changes and completions. Introducing
 * this path requires a schema/topic and an explicit failure/delivery policy.
 */
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
