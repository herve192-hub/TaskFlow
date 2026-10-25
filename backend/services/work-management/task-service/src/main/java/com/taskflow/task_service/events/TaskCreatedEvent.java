package com.taskflow.task_service.events;

import com.taskflow.task_service.domain.enums.TaskPriority;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.domain.enums.TaskType;

import java.time.Instant;

/**
 * Declared task-created event shape with task/project/actor metadata.
 * Current createTask persists and returns the entity without publishing this event. Consumers
 * must not assume a creation topic, notification, or reliable delivery pipeline is implemented.
 */
public record TaskCreatedEvent(

        String taskId,

        String projectId,

        String title,

        String createdBy,

        String assigneeId,

        TaskStatus status,

        TaskPriority priority,

        TaskType type,

        Instant createdAt

) {
}
