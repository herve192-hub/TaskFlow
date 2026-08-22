package com.taskflow.task_service.controller;

import com.taskflow.task_service.dto.request.AddTaskCommentRequest;
import com.taskflow.task_service.dto.request.CreateTaskRequest;
import com.taskflow.task_service.dto.request.UpdateTaskRequest;
import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.dto.response.TaskCommentResponse;
import com.taskflow.task_service.dto.response.TaskResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;
import com.taskflow.task_service.service.interfaces.TaskService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        TaskResponse response =
                taskService.createTask(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get a task by ID.
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable String taskId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getTaskById(taskId, userId)
        );
    }

    /**
     * Update a task.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.updateTask(
                        taskId,
                        request,
                        userId
                )
        );
    }

    /**
     * Soft delete a task.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        taskService.deleteTask(taskId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get all active tasks belonging to a project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getProjectTasks(
            @PathVariable String projectId,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getTasksByProject(
                        projectId,
                        pageable,
                        userId
                )
        );
    }

    /**
     * Get the currently authenticated user's assigned tasks.
     */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getMyTasks(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        pageable,
                        userId
                )
        );
    }

    /**
     * Get tasks created by the authenticated user.
     */
    @GetMapping("/created-by-me")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getCreatedTasks(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getCreatedTasks(
                        pageable,
                        userId
                )
        );
    }

    /**
     * Get project tasks filtered by status.
     */
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksByStatus(
            @PathVariable String projectId,
            @PathVariable String status,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getTasksByProjectAndStatus(
                        projectId,
                        status,
                        pageable,
                        userId
                )
        );
    }

    /**
     * Assign a task to a user.
     *
     * Example:
     *
     * PUT /api/v1/tasks/{taskId}/assignee/{assigneeId}
     */
    @PutMapping("/{taskId}/assignee/{assigneeId}")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable String taskId,
            @PathVariable String assigneeId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.assignTask(
                        taskId,
                        assigneeId,
                        userId
                )
        );
    }

    /**
     * Archive a task.
     */
    @PutMapping("/{taskId}/archive")
    public ResponseEntity<TaskResponse> archiveTask(
            @PathVariable String taskId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.archiveTask(
                        taskId,
                        userId
                )
        );
    }

    /**
     * Unarchive a task.
     */
    @PutMapping("/{taskId}/unarchive")
    public ResponseEntity<TaskResponse> unarchiveTask(
            @PathVariable String taskId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.unarchiveTask(
                        taskId,
                        userId
                )
        );
    }

    // =========================================================
    // COMMENTS
    // =========================================================

    /**
     * Add a comment to a task.
     */
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentResponse> addComment(
            @PathVariable String taskId,

            @Valid
            @RequestBody
            AddTaskCommentRequest request,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        TaskCommentResponse response =
                taskService.addComment(
                        taskId,
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get comments for a task.
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<PageResponse<TaskCommentResponse>> getComments(
            @PathVariable String taskId,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.getComments(
                        taskId,
                        pageable,
                        userId
                )
        );
    }

    // =========================================================
    // PROJECT STATISTICS
    // =========================================================

    /**
     * Count active tasks in a project.
     */
    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<Long> countProjectTasks(
            @PathVariable String projectId,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.countProjectTasks(
                        projectId,
                        userId
                )
        );
    }

    /**
     * Count active tasks in a project by status.
     */
    @GetMapping("/project/{projectId}/count/{status}")
    public ResponseEntity<Long> countProjectTasksByStatus(
            @PathVariable String projectId,
            @PathVariable String status,
            Authentication authentication
    ) {
        String userId = getUserId(authentication);

        return ResponseEntity.ok(
                taskService.countProjectTasksByStatus(
                        projectId,
                        status,
                        userId
                )
        );
    }

    // =========================================================
    // AUTHENTICATION HELPER
    // =========================================================

    private String getUserId(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        return authentication.getName();
    }
}