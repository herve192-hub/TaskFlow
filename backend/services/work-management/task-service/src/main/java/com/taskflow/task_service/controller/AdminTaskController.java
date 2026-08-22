package com.taskflow.task_service.controller;

import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.dto.response.TaskSummaryResponse;
import com.taskflow.task_service.service.interfaces.TaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminTaskController {

    private final TaskService taskService;

    /**
     * Get all tasks created by a specific user.
     *
     * This endpoint is intended for administrators.
     */
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksCreatedByUser(
            @PathVariable String userId,

            @PageableDefault(
                    size = 50,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                taskService.getCreatedTasks(
                        pageable,
                        userId
                )
        );
    }

    /**
     * Get all tasks assigned to a specific user.
     */
    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksAssignedToUser(
            @PathVariable String userId,

            @PageableDefault(
                    size = 50,
                    sort = "createdAt"
            )
            Pageable pageable,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        pageable,
                        userId
                )
        );
    }

    /**
     * Count all active tasks belonging to a project.
     */
    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<Long> countProjectTasks(
            @PathVariable String projectId
    ) {

        return ResponseEntity.ok(
                taskService.countProjectTasks(
                        projectId,
                        "ADMIN"
                )
        );
    }

    /**
     * Count project tasks by status.
     */
    @GetMapping("/project/{projectId}/count/{status}")
    public ResponseEntity<Long> countProjectTasksByStatus(
            @PathVariable String projectId,
            @PathVariable String status
    ) {

        return ResponseEntity.ok(
                taskService.countProjectTasksByStatus(
                        projectId,
                        status,
                        "ADMIN"
                )
        );
    }
}