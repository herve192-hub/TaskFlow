package com.taskflow.task_service.service.interfaces;

import com.taskflow.task_service.dto.request.AddTaskCommentRequest;
import com.taskflow.task_service.dto.request.CreateTaskRequest;
import com.taskflow.task_service.dto.request.UpdateTaskRequest;
import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.dto.response.TaskCommentResponse;
import com.taskflow.task_service.dto.response.TaskResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;

import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(
            CreateTaskRequest request,
            String userId
    );

    TaskResponse getTaskById(
            String taskId,
            String userId
    );

    TaskResponse updateTask(
            String taskId,
            UpdateTaskRequest request,
            String userId
    );

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

    TaskResponse assignTask(
            String taskId,
            String assigneeId,
            String userId
    );

    TaskResponse archiveTask(
            String taskId,
            String userId
    );

    TaskResponse unarchiveTask(
            String taskId,
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