package com.taskflow.task_service.events.publisher.kafka.message;

import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.events.TaskStatusChangedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * External schema-version-1 status-change message built from the internal domain event.
 * Includes previous/new status and canonical actor with a fresh eventId; Kafka uses taskId as key.
 * Changing enum values or field meaning affects downstream consumers even when HTTP DTOs compile.
 */
public record TaskStatusChangedMessage(
        String eventId,
        String eventType,
        int schemaVersion,
        String taskId,
        String projectId,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        String changedBy,
        Instant occurredAt
) {

    private static final String EVENT_TYPE =
            "task.status.changed";

    private static final int SCHEMA_VERSION = 1;

    public static TaskStatusChangedMessage from(
            TaskStatusChangedEvent event
    ) {

        return new TaskStatusChangedMessage(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                SCHEMA_VERSION,
                event.taskId(),
                event.projectId(),
                event.previousStatus(),
                event.newStatus(),
                event.changedBy(),
                event.changedAt()
        );
    }
}
