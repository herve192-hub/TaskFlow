package com.taskflow.task_service.mapper;

import com.taskflow.task_service.domain.Task;
import com.taskflow.task_service.domain.TaskComment;
import com.taskflow.task_service.dto.request.AddTaskCommentRequest;
import com.taskflow.task_service.dto.request.CreateTaskRequest;
import com.taskflow.task_service.dto.request.UpdateTaskRequest;
import com.taskflow.task_service.dto.response.TaskCommentResponse;
import com.taskflow.task_service.dto.response.TaskResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class TaskMapper {

    public Task toEntity(
            CreateTaskRequest request,
            String createdBy
    ) {

        return Task.builder()
                .projectId(request.getProjectId())
                .createdBy(createdBy)
                .assigneeId(request.getAssigneeId())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .type(request.getType())
                .parentTaskId(request.getParentTaskId())
                .sprintId(request.getSprintId())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .tags(request.getTags() != null
                        ? new ArrayList<>(request.getTags())
                        : new ArrayList<>())
                .build();
    }

    public void updateEntity(
            Task task,
            UpdateTaskRequest request
    ) {

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getType() != null) {
            task.setType(request.getType());
        }

        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }

        if (request.getSprintId() != null) {
            task.setSprintId(request.getSprintId());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }

        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        if (request.getTags() != null) {
            task.setTags(new ArrayList<>(request.getTags()));
        }
    }

    public TaskResponse toResponse(Task task) {

        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .createdBy(task.getCreatedBy())
                .assigneeId(task.getAssigneeId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .parentTaskId(task.getParentTaskId())
                .sprintId(task.getSprintId())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .tags(task.getTags())
                .archived(task.isArchived())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public TaskSummaryResponse toSummaryResponse(Task task) {

        return TaskSummaryResponse.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .assigneeId(task.getAssigneeId())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .build();
    }

    public TaskComment toCommentEntity(
            AddTaskCommentRequest request,
            String taskId,
            String authorId
    ) {

        return TaskComment.builder()
                .taskId(taskId)
                .authorId(authorId)
                .content(request.getContent())
                .build();
    }

    public TaskCommentResponse toCommentResponse(
            TaskComment comment
    ) {

        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .authorId(comment.getAuthorId())
                .content(comment.getContent())
                .edited(comment.isEdited())
                .editedAt(comment.getEditedAt())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}