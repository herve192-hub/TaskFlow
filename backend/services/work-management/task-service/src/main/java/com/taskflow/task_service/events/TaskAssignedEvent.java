package com.taskflow.task_service.events;

import java.time.Instant;

/**
 * Declared assignment-event record reserved for a future publication path.
 * Current assignTask/updateTask persist assignment changes but do not emit this record; the
 * configured Kafka publisher currently handles only status-change and completion events.
 */
public record TaskAssignedEvent(

        String taskId,

        String projectId,

        String previousAssigneeId,

        String assigneeId,

        String assignedBy,

        Instant assignedAt

) {
}
