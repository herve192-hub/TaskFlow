package com.taskflow.task_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Comment projection containing task/author IDs, content, and audit/edit metadata.
 * Author identity is a canonical auth ID resolved separately for display; the response does not
 * embed profile data or grant permission to mutate the referenced task.
 */
@Data
@Builder
public class TaskCommentResponse {

    private String id;

    private String taskId;

    private String authorId;

    private String content;

    private boolean edited;

    private Instant editedAt;

    private Instant createdAt;
}
