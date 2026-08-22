package com.taskflow.task_service.service.impl;

import com.taskflow.task_service.domain.Task;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.dto.request.AddTaskCommentRequest;
import com.taskflow.task_service.dto.request.CreateTaskRequest;
import com.taskflow.task_service.dto.request.UpdateTaskRequest;
import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.dto.response.TaskCommentResponse;
import com.taskflow.task_service.dto.response.TaskResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;
import com.taskflow.task_service.exception.DuplicateTaskException;
import com.taskflow.task_service.exception.InvalidTaskException;
import com.taskflow.task_service.exception.TaskAccessDeniedException;
import com.taskflow.task_service.exception.TaskNotFoundException;
import com.taskflow.task_service.mapper.TaskMapper;
import com.taskflow.task_service.repository.TaskCommentRepository;
import com.taskflow.task_service.repository.TaskRepository;
import com.taskflow.task_service.service.interfaces.TaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponse createTask(
            CreateTaskRequest request,
            String userId
    ) {
        if (request == null) {
            throw new InvalidTaskException("Task request cannot be null");
        }

        if (userId == null || userId.isBlank()) {
            throw new InvalidTaskException("Authenticated user is required");
        }

        boolean duplicate = taskRepository
                .existsByProjectIdAndTitleIgnoreCaseAndArchivedFalse(
                        request.getProjectId(),
                        request.getTitle()
                );

        if (duplicate) {
            throw new DuplicateTaskException(
                    "A task with this title already exists in the project"
            );
        }

        Task task = taskMapper.toEntity(request, userId);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(
            String taskId,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyAccess(task, userId);

        return taskMapper.toResponse(task);
    }

    @Override
    public TaskResponse updateTask(
            String taskId,
            UpdateTaskRequest request,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyModificationAccess(task, userId);

        if (request == null) {
            throw new InvalidTaskException("Update request cannot be null");
        }

        taskMapper.updateEntity(task, request);

        updateCompletionTimestamp(task);

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public void deleteTask(
            String taskId,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyModificationAccess(task, userId);

        /*
         * TaskFlow uses soft deletion.
         */
        task.setArchived(true);

        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getTasksByProject(
            String projectId,
            Pageable pageable,
            String userId
    ) {
        validateProjectId(projectId);

        Page<TaskSummaryResponse> page = taskRepository
                .findByProjectIdAndArchivedFalse(projectId, pageable)
                .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getMyTasks(
            Pageable pageable,
            String userId
    ) {
        validateUserId(userId);

        Page<TaskSummaryResponse> page = taskRepository
                .findByAssigneeIdAndArchivedFalse(userId, pageable)
                .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getCreatedTasks(
            Pageable pageable,
            String userId
    ) {
        validateUserId(userId);

        Page<TaskSummaryResponse> page = taskRepository
                .findByCreatedByAndArchivedFalse(userId, pageable)
                .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getTasksByProjectAndStatus(
            String projectId,
            String status,
            Pageable pageable,
            String userId
    ) {
        validateProjectId(projectId);

        TaskStatus taskStatus = parseStatus(status);

        Page<TaskSummaryResponse> page = taskRepository
                .findByProjectIdAndStatusAndArchivedFalse(
                        projectId,
                        taskStatus,
                        pageable
                )
                .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    public TaskResponse assignTask(
            String taskId,
            String assigneeId,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyModificationAccess(task, userId);

        if (assigneeId == null || assigneeId.isBlank()) {
            throw new InvalidTaskException(
                    "Assignee ID cannot be empty"
            );
        }

        task.setAssigneeId(assigneeId);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse archiveTask(
            String taskId,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyModificationAccess(task, userId);

        task.setArchived(true);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse unarchiveTask(
            String taskId,
            String userId
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task not found: " + taskId
                        )
                );

        verifyModificationAccess(task, userId);

        task.setArchived(false);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskCommentResponse addComment(
            String taskId,
            AddTaskCommentRequest request,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyAccess(task, userId);

        if (request == null ||
                request.getContent() == null ||
                request.getContent().isBlank()) {

            throw new InvalidTaskException(
                    "Comment content cannot be empty"
            );
        }

        var comment = taskMapper.toCommentEntity(
                request,
                taskId,
                userId
        );

        var savedComment = taskCommentRepository.save(comment);

        return taskMapper.toCommentResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskCommentResponse> getComments(
            String taskId,
            Pageable pageable,
            String userId
    ) {
        Task task = findTask(taskId);

        verifyAccess(task, userId);

        Page<TaskCommentResponse> page =
                taskCommentRepository
                        .findByTaskIdOrderByCreatedAtDesc(taskId, pageable)
                        .map(taskMapper::toCommentResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectTasks(
            String projectId,
            String userId
    ) {
        validateProjectId(projectId);

        return taskRepository
                .countByProjectIdAndArchivedFalse(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectTasksByStatus(
            String projectId,
            String status,
            String userId
    ) {
        validateProjectId(projectId);

        return taskRepository.countByProjectIdAndStatusAndArchivedFalse(
                projectId,
                parseStatus(status)
        );
    }

    private Task findTask(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new InvalidTaskException(
                    "Task ID cannot be empty"
            );
        }

        return taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task not found: " + taskId
                        )
                );
    }

    private void verifyAccess(
            Task task,
            String userId
    ) {
        validateUserId(userId);

        boolean owner = userId.equals(task.getCreatedBy());
        boolean assignee = userId.equals(task.getAssigneeId());

        if (!owner && !assignee) {
            /*
             * Project membership should ultimately be verified
             * through project-service. For now, task ownership
             * and assignment are the local checks.
             */
            throw new TaskAccessDeniedException(
                    "You do not have access to this task"
            );
        }
    }

    private void verifyModificationAccess(
            Task task,
            String userId
    ) {
        validateUserId(userId);

        if (!userId.equals(task.getCreatedBy())) {
            throw new TaskAccessDeniedException(
                    "Only the task creator can modify this task"
            );
        }
    }

    private void updateCompletionTimestamp(Task task) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    private TaskStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new InvalidTaskException(
                    "Task status cannot be empty"
            );
        }

        try {
            return TaskStatus.valueOf(
                    status.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTaskException(
                    "Invalid task status: " + status
            );
        }
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new InvalidTaskException(
                    "Project ID cannot be empty"
            );
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidTaskException(
                    "Authenticated user is required"
            );
        }
    }
}