package com.taskflow.task_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

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