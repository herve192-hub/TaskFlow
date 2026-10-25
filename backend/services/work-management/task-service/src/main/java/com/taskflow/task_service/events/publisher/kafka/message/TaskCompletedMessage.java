package com.taskflow.task_service.events.publisher.kafka.message;

import com.taskflow.task_service.events.TaskCompletedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * External schema-version-1 completion message built from TaskCompletedEvent.
 * Adds eventType and a newly generated eventId plus occurrence data for Kafka consumers.
 * Consumers must manage schema compatibility/idempotency themselves; reconstructing a message
 * creates a different UUID and does not constitute stable retry deduplication.
 */
public record TaskCompletedMessage(
        String eventId,
        String eventType,
        int schemaVersion,
        String taskId,
        String projectId,
        String completedBy,
        String assigneeId,
        Double estimatedHours,
        Double actualHours,
        Instant occurredAt
) {

    private static final String EVENT_TYPE =
            "task.completed";

    private static final int SCHEMA_VERSION = 1;

    public static TaskCompletedMessage from(
            TaskCompletedEvent event
    ) {

        return new TaskCompletedMessage(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                SCHEMA_VERSION,
                event.taskId(),
                event.projectId(),
                event.completedBy(),
                event.assigneeId(),
                event.estimatedHours(),
                event.actualHours(),
                event.completedAt()
        );
    }
}
