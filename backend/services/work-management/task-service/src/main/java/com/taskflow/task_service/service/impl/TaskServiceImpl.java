package com.taskflow.task_service.service.impl;

import com.taskflow.task_service.client.ProjectServiceClient;
import com.taskflow.task_service.client.UserServiceClient;
import com.taskflow.task_service.client.dto.ProjectAccessResponse;

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
import com.taskflow.task_service.exception.ProjectServiceUnavailableException;
import com.taskflow.task_service.exception.TaskAccessDeniedException;
import com.taskflow.task_service.exception.TaskNotFoundException;
import com.taskflow.task_service.exception.UserServiceUnavailableException;

import com.taskflow.task_service.mapper.TaskMapper;

import com.taskflow.task_service.repository.TaskCommentRepository;
import com.taskflow.task_service.repository.TaskRepository;

import com.taskflow.task_service.service.interfaces.TaskService;

import feign.RetryableException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskCommentRepository taskCommentRepository;

    private final TaskMapper taskMapper;

    private final ProjectServiceClient projectServiceClient;

    private final UserServiceClient userServiceClient;


    // =========================================================
    // TASK CREATION
    // =========================================================

    @Override
    public TaskResponse createTask( CreateTaskRequest request,  String userId ) {

        validateUserId(userId);

        if (request == null) {
            throw new InvalidTaskException( "Task request cannot be null" );
        }
        validateProjectId( request.getProjectId() );
        /*
         * User must belong to the project and have
         * permission to create tasks.
         */
        ensureTaskCreationAccess( request.getProjectId() );
        /*
         * Validate assignee when one is supplied.
         */
        if (isNotBlank(request.getAssigneeId())) {
            validateAssignee( request.getProjectId(), request.getAssigneeId() );
        }

        boolean duplicate =
                taskRepository
                        .existsByProjectIdAndTitleIgnoreCaseAndArchivedFalse(
                                request.getProjectId(), request.getTitle() );

        if (duplicate) {
            throw new DuplicateTaskException( "A task with this title already exists in the project" );
        }
        Task task = taskMapper.toEntity( request, userId );
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse( savedTask );
    }

    // =========================================================
    // TASK RETRIEVAL
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById( String taskId, String userId ) {

        validateUserId(userId);
        Task task = findActiveTask(taskId);
        verifyAccess( task, userId );

        return taskMapper.toResponse(task);
    }

    // =========================================================
    // TASK UPDATE
    // =========================================================
    @Override
    public TaskResponse updateTask( String taskId, UpdateTaskRequest request, String userId ) {

        validateUserId(userId);

        if (request == null) {
            throw new InvalidTaskException( "Update request cannot be null" );
        }
        Task task = findActiveTask(taskId);

        verifyModificationAccess( task, userId );
        /*
         * Generic task update must not allow a regular
         * project member to bypass assignTask().
         *
         * If assigneeId is changing, apply assignment
         * permissions and assignee validation.
         */
        handleAssigneeUpdate( task, request, userId );

        taskMapper.updateEntity( task, request );

        updateCompletionTimestamp(task);

        Task updatedTask =
                taskRepository.save(task);

        return taskMapper.toResponse( updatedTask );
    }

    // =========================================================
    // DELETE / ARCHIVE
    // =========================================================
    @Override
    public void deleteTask( String taskId, String userId ) {

        validateUserId(userId);

        Task task = findActiveTask(taskId);

        verifyModificationAccess( task, userId );
        /*
         * TaskFlow currently uses soft deletion.
         */
        task.setArchived(true);
        taskRepository.save(task);
    }

    @Override
    public TaskResponse archiveTask( String taskId, String userId ) {

        validateUserId(userId);

        Task task =  findActiveTask(taskId);

        verifyModificationAccess( task, userId );

        task.setArchived(true);

        return taskMapper.toResponse( taskRepository.save(task) );
    }

    @Override
    public TaskResponse unarchiveTask( String taskId, String userId ) {

        validateUserId(userId);
        /*
         * Unlike normal retrieval, this intentionally allows
         * finding an archived task.
         */
        Task task = findTaskIncludingArchived(taskId);

        if (!task.isArchived()) {
            throw new InvalidTaskException( "Task is not archived" );
        }
        /*
         * Restoring an archived task should be treated as a
         * management operation.
         */
        ensureTaskManagementAccess( task.getProjectId() );

        task.setArchived(false);

        return taskMapper.toResponse( taskRepository.save(task) );
    }

    // =========================================================
    // PROJECT TASK LISTS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getTasksByProject( String projectId, Pageable pageable, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        ensureProjectViewAccess(projectId);

        Page<TaskSummaryResponse> page =
                taskRepository
                        .findByProjectIdAndArchivedFalse( projectId, pageable )
                        .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getTasksByProjectAndStatus(
            String projectId, String status, Pageable pageable, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        ensureProjectViewAccess(projectId);

        TaskStatus taskStatus = parseStatus(status);

        Page<TaskSummaryResponse> page =
                taskRepository
                        .findByProjectIdAndStatusAndArchivedFalse( projectId, taskStatus, pageable )
                        .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    // =========================================================
    // USER TASK LISTS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getMyTasks( Pageable pageable, String userId ) {

        validateUserId(userId);

        Page<TaskSummaryResponse> page = taskRepository
                        .findByAssigneeIdAndArchivedFalse( userId, pageable )
                        .map(taskMapper::toSummaryResponse);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getCreatedTasks( Pageable pageable, String userId ) {

        validateUserId(userId);

        Page<TaskSummaryResponse> page = taskRepository
                        .findByCreatedByAndArchivedFalse( userId, pageable )
                        .map(taskMapper::toSummaryResponse);
        return PageResponse.from(page);
    }

    // =========================================================
    // TASK ASSIGNMENT
    // =========================================================
    @Override
    public TaskResponse assignTask( String taskId, String assigneeId, String userId ) {

        validateUserId(userId);
        validateUserId(assigneeId);

        Task task = findActiveTask(taskId);
        /*
         * Assignment is a project-management operation.
         */
        ensureTaskManagementAccess( task.getProjectId() );

        validateAssignee( task.getProjectId(), assigneeId );

        task.setAssigneeId( assigneeId );
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse( savedTask );
    }

    // =========================================================
    // COMMENTS
    // =========================================================
    @Override
    public TaskCommentResponse addComment( String taskId, AddTaskCommentRequest request, String userId ) {

        validateUserId(userId);
        Task task = findActiveTask(taskId);
        verifyAccess( task, userId );

        if (request == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new InvalidTaskException( "Comment content cannot be empty");
        }
        var comment = taskMapper.toCommentEntity( request, taskId, userId );
        var savedComment = taskCommentRepository.save(comment);

        return taskMapper.toCommentResponse( savedComment );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskCommentResponse> getComments( String taskId, Pageable pageable, String userId ) {

        validateUserId(userId);
        Task task = findActiveTask(taskId);
        verifyAccess( task, userId );

        Page<TaskCommentResponse> page = taskCommentRepository
                        .findByTaskIdOrderByCreatedAtDesc(taskId, pageable )
                        .map(taskMapper::toCommentResponse);

        return PageResponse.from(page);
    }

    // =========================================================
    // PROJECT STATISTICS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public long countProjectTasks( String projectId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);
        ensureProjectViewAccess(projectId);

        return taskRepository
                .countByProjectIdAndArchivedFalse( projectId );
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectTasksByStatus( String projectId, String status, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);
        ensureProjectViewAccess(projectId);

        return taskRepository
                .countByProjectIdAndStatusAndArchivedFalse( projectId, parseStatus(status));
    }

    // =========================================================
    // TASK LOOKUP HELPERS
    // =========================================================
    private Task findActiveTask(String taskId) {

        Task task = findTaskIncludingArchived(taskId);

        if (task.isArchived()) {
            throw new TaskNotFoundException( "Task not found: " + taskId); 
        }
        return task;
    }

    private Task findTaskIncludingArchived( String taskId) {

        validateTaskId(taskId);

        return taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new TaskNotFoundException( "Task not found: " + taskId ) );
    }

    // =========================================================
    // TASK AUTHORIZATION
    // =========================================================
    private void verifyAccess( Task task, String userId) {

        validateUserId(userId);

        ensureProjectViewAccess( task.getProjectId() );
    }

    private void verifyModificationAccess( Task task, String userId ) {

        validateUserId(userId);

        ProjectAccessResponse access = getProjectAccess( task.getProjectId() );

        if (!access.isMember()) {
            throw new TaskAccessDeniedException("You are not a member of this project");
        }

        String role = access.getRole();
        /*
         * Project leadership can modify any task.
         */
        if (isManagementRole(role)) {
            return;
        }
        /*
         * Guests are strictly read-only.
         */
        if ("GUEST".equals(role)) {
            throw new TaskAccessDeniedException("Guests cannot modify project tasks");
        }
        /*
         * Regular members may modify tasks they created
         * or tasks assigned to them.
         */
        if ("MEMBER".equals(role)) {

            boolean creator = Objects.equals( userId, task.getCreatedBy());
            boolean assignee = Objects.equals(userId, task.getAssigneeId());

            if (creator || assignee) {
                return;
            }
        }

        throw new TaskAccessDeniedException("You do not have permission to modify this task");
    }

    private void ensureProjectViewAccess( String projectId ) {

        ProjectAccessResponse access = getProjectAccess(projectId);

        if (!access.isMember() || !access.isCanView()) {
            throw new TaskAccessDeniedException("You do not have access to this project");
        }
    }

    private void ensureTaskCreationAccess(String projectId ) {

        ProjectAccessResponse access = getProjectAccess(projectId);

        if (!access.isMember()) {
            throw new TaskAccessDeniedException( "You are not a member of this project" );
        }

        String role = access.getRole();

        boolean allowed =
                "OWNER".equals(role)
                        || "ADMIN".equals(role)
                        || "MANAGER".equals(role)
                        || "MEMBER".equals(role);

        if (!allowed) {
            throw new TaskAccessDeniedException( "You do not have permission to create tasks" );
        }
    }

    private void ensureTaskManagementAccess(String projectId ) {

        if (!hasTaskManagementAccess(projectId)) {

            throw new TaskAccessDeniedException( "You do not have permission to manage this task" );
        }
    }

    private boolean hasTaskManagementAccess(String projectId) {

        ProjectAccessResponse access = getProjectAccess(projectId);

        return access.isMember() && isManagementRole( access.getRole() );
    }

    private boolean isManagementRole(String role) {

        return "OWNER".equals(role) || "ADMIN".equals(role) || "MANAGER".equals(role);
    }

    // =========================================================
    // ASSIGNEE BUSINESS RULES
    // =========================================================
    private void validateAssignee( String projectId, String assigneeId ) {

        validateUserId(assigneeId);

        if (!userExists(assigneeId)) {
            throw new InvalidTaskException( "Assignee does not exist" );
        }
        if (!isProjectMember( projectId, assigneeId)) {
            throw new InvalidTaskException( "Assignee is not a member of this project" );
        }
    }

    private void handleAssigneeUpdate( Task task, UpdateTaskRequest request,String userId ) {
        
        String requestedAssignee = request.getAssigneeId();

        if (requestedAssignee == null) {
            return;
        }
        /*
         * Nothing changed.
         */
        if (Objects.equals( requestedAssignee, task.getAssigneeId() )) {
            return;
        }
        /*
         * Assignee changes require project-management
         * permission even when using the general update API.
         */
        ensureTaskManagementAccess( task.getProjectId() );

        if (!requestedAssignee.isBlank()) {
            validateAssignee( task.getProjectId(), requestedAssignee );
        }
    }

    // =========================================================
    // TASK STATUS HELPERS
    // =========================================================
    private void updateCompletionTimestamp( Task task ) {

        if (task.getStatus() == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt( Instant.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    private TaskStatus parseStatus( String status) {

        if (status == null || status.isBlank()) {
            throw new InvalidTaskException( "Task status cannot be empty" );
        }
        try {

            return TaskStatus.valueOf(
                    status
                            .trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException exception) {
            throw new InvalidTaskException( "Invalid task status: " + status );
        }
    }

    // =========================================================
    // DOWNSTREAM SERVICE HELPERS
    // =========================================================
    private ProjectAccessResponse getProjectAccess( String projectId) {

        validateProjectId(projectId);

        try {
            ProjectAccessResponse access = projectServiceClient
                            .getProjectAccess(projectId );

            if (access == null) {
                throw new ProjectServiceUnavailableException( "Project service returned an invalid response" );
            }
            return access;

        } catch (RetryableException exception) {

            throw new ProjectServiceUnavailableException( "Project service is temporarily unavailable", exception );
        }
    }

    private boolean userExists(String userId
    ) {
        try {
            return userServiceClient
                    .userExists(userId);
        } catch (RetryableException exception) {

            throw new UserServiceUnavailableException( "User service is temporarily unavailable", exception );
        }
    }

    private boolean isProjectMember(String projectId,String userId) {

        try {
            return projectServiceClient
                    .isProjectMember( projectId,userId );
        } catch (RetryableException exception) {
            throw new ProjectServiceUnavailableException( "Project service is temporarily unavailable", exception );
        }
    }

    // =========================================================
    // BASIC VALIDATION
    // =========================================================
    private void validateTaskId( String taskId) {
        if (taskId == null|| taskId.isBlank()) {
            throw new InvalidTaskException( "Task ID cannot be empty");
        }
    }

    private void validateProjectId( String projectId ) {
        if (projectId == null|| projectId.isBlank()) {
            throw new InvalidTaskException( "Project ID cannot be empty" );
        }
    }

    private void validateUserId( String userId ) {
        if (userId == null || userId.isBlank()) {
            throw new InvalidTaskException( "User ID cannot be empty" );
        }
    }

    private boolean isNotBlank(  String value ) {
        return value != null && !value.isBlank();
    }
}