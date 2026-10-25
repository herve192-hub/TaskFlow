package com.taskflow.task_service.events;

import java.time.Instant;

/**
 * Internal event emitted when a task enters COMPLETED, alongside the status-change event.
 * Includes completion timestamp and effort/assignee metadata captured from the saved task.
 * TaskDomainEventListener selects its external publisher; reopening clears completedAt but does
 * not emit another completion event until a later valid completion.
 */
public record TaskCompletedEvent(

        String taskId,

        String projectId,

        String completedBy,

        String assigneeId,

        Double estimatedHours,

        Double actualHours,

        Instant completedAt

) {
}
