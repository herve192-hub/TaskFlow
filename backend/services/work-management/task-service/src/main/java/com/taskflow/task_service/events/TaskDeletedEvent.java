package com.taskflow.task_service.events;

import java.time.Instant;

public record TaskDeletedEvent(

        String taskId,

        String projectId,

        String deletedBy,

        Instant deletedAt

) {
}