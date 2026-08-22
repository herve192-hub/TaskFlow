package com.taskflow.task_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddTaskCommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    private String content;
}