package com.taskflow.task_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "task_comments")
public class TaskComment {

    @Id
    private String id;

    private String taskId;

    private String authorId;

    private String content;

    @Builder.Default
    private boolean edited = false;

    private Instant editedAt;

    @CreatedDate
    private Instant createdAt;
}