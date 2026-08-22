package com.taskflow.task_service.events;

import java.time.Instant;

public record TaskAssignedEvent(

        String taskId,

        String projectId,

        String previousAssigneeId,

        String assigneeId,

        String assignedBy,

        Instant assignedAt

) {
}