package com.taskflow.task_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Bounded nonblank comment input; the caller cannot choose authorId or taskId in the body.
 * TaskController takes the task from the route and TaskServiceImpl derives the author from the
 * signed caller, checking task/project view access before persistence.
 */
@Data
public class AddTaskCommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    private String content;
}
