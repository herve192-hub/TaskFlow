package com.taskflow.task_service.events;

import java.time.Instant;

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