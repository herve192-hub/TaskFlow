package com.taskflow.task_service.service.interfaces;

import com.taskflow.task_service.dto.request.AddTaskCommentRequest;
import com.taskflow.task_service.dto.request.CreateTaskRequest;
import com.taskflow.task_service.dto.request.UpdateTaskRequest;
import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.dto.response.TaskCommentResponse;
import com.taskflow.task_service.dto.response.TaskResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;

import com.taskflow.task_service.dto.request.ChangeTaskStatusRequest;

import org.springframework.data.domain.Pageable;

/**
 * Application contract for task metadata, project/personal queries, assignment, comments, and lifecycle.
 * Callers pass canonical actor IDs from authentication. The implementation owns permission checks,
 * repository operations, completion timestamps, and domain events; this interface does not expose
 * password management, arbitrary project role assignment, or an external transaction boundary.
 */
public interface TaskService {

    TaskResponse createTask(
            CreateTaskRequest request,
            String userId
    );

    TaskResponse getTaskById(
            String taskId,
            String userId
    );

    /** Update supplied fields; assignment changes still require project-management permission. */
    TaskResponse updateTask(
            String taskId,
            UpdateTaskRequest request,
            String userId
    );

    /** Soft-delete by archiving; the task document and its comments are retained. */
    void deleteTask(
            String taskId,
            String userId
    );

    PageResponse<TaskSummaryResponse> getTasksByProject(
            String projectId,
            Pageable pageable,
            String userId
    );

    PageResponse<TaskSummaryResponse> getMyTasks(
            Pageable pageable,
            String userId
    );

    PageResponse<TaskSummaryResponse> getCreatedTasks(
            Pageable pageable,
            String userId
    );

    PageResponse<TaskSummaryResponse> getTasksByProjectAndStatus(
            String projectId,
            String status,
            Pageable pageable,
            String userId
    );

    /** Assign an existing project member; the acting caller must have a management project role. */
    TaskResponse assignTask(
            String taskId,
            String assigneeId,
            String userId
    );

    TaskResponse archiveTask(
            String taskId,
            String userId
    );

    /** Restore an archived task through the management-only path. */
    TaskResponse unarchiveTask(
            String taskId,
            String userId
    );

    /** Validate lifecycle and caller permissions, update completion time, and publish domain events. */
    TaskResponse changeTaskStatus(
            String taskId,
            ChangeTaskStatusRequest request,
            String userId
    );

    TaskCommentResponse addComment(
            String taskId,
            AddTaskCommentRequest request,
            String userId
    );

    PageResponse<TaskCommentResponse> getComments(
            String taskId,
            Pageable pageable,
            String userId
    );

    long countProjectTasks(
            String projectId,
            String userId
    );

    long countProjectTasksByStatus(
            String projectId,
            String status,
            String userId
    );
}
