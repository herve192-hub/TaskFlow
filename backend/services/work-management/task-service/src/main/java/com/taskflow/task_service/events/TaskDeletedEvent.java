package com.taskflow.task_service.events;

import java.time.Instant;

/**
 * Declared deletion/archive event shape reserved for future integration.
 * Current deleteTask soft-archives the document and does not publish this record or physically
 * remove task/comment data. It must not be treated as an existing Kafka deletion contract.
 */
public record TaskDeletedEvent(

        String taskId,

        String projectId,

        String deletedBy,

        Instant deletedAt

) {
}
