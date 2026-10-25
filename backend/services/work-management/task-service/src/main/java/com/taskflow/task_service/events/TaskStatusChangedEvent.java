package com.taskflow.task_service.events;

import com.taskflow.task_service.domain.enums.TaskStatus;

import java.time.Instant;

/**
 * Internal immutable domain event emitted after a successful task status save.
 * Carries previous/new state, canonical actor, and occurrence time; TaskDomainEventListener turns
 * it into the external Kafka schema through TaskEventPublisher. It is not itself a broker record.
 */
public record TaskStatusChangedEvent(

        String taskId,

        String projectId,

        TaskStatus previousStatus,

        TaskStatus newStatus,

        String changedBy,

        Instant changedAt

) {
}
