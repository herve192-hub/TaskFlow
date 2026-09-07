package com.taskflow.task_service.service.impl;

import com.taskflow.task_service.client.ProjectServiceClient;
import com.taskflow.task_service.client.UserServiceClient;
import com.taskflow.task_service.client.dto.ProjectAccessResponse;
import com.taskflow.task_service.domain.Task;
import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.dto.request.ChangeTaskStatusRequest;
import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;
import com.taskflow.task_service.mapper.TaskMapper;
import com.taskflow.task_service.repository.TaskCommentRepository;
import com.taskflow.task_service.repository.TaskRepository;
import com.taskflow.task_service.service.policy.TaskLifecyclePolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ProjectServiceClient projectServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TaskLifecyclePolicy taskLifecyclePolicy;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {

        taskLifecyclePolicy =
                new TaskLifecyclePolicy();

        taskService =
                new TaskServiceImpl(
                        taskRepository,
                        taskCommentRepository,
                        taskMapper,
                        projectServiceClient,
                        userServiceClient,
                        taskLifecyclePolicy,
                        eventPublisher
                );
    }

    private ProjectAccessResponse memberAccess(
            String userId
    ) {

        return ProjectAccessResponse.builder()
                .projectId("project-1")
                .userId(userId)
                .member(true)
                .role("MEMBER")
                .canView(true)
                .canEdit(false)
                .canManageMembers(false)
                .build();
    }

    private ProjectAccessResponse managerAccess(
            String userId
    ) {

        return ProjectAccessResponse.builder()
                .projectId("project-1")
                .userId(userId)
                .member(true)
                .role("MANAGER")
                .canView(true)
                .canEdit(true)
                .canManageMembers(false)
                .build();
    }

    private Task task(
            TaskStatus status
    ) {

        return Task.builder()
                .id("task-1")
                .projectId("project-1")
                .createdBy("creator-1")
                .assigneeId("member-1")
                .title("Lifecycle test")
                .status(status)
                .archived(false)
                .build();
    }

    // Test for changing task status from TODO to IN_PROGRESS by the assigned member...
    @Test
    void assignedMemberShouldMoveTodoToInProgress() {

        Task task = task(TaskStatus.TODO);

        when(taskRepository.findById("task-1"))
                .thenReturn(Optional.of(task));

        when(projectServiceClient.getProjectAccess("project-1"))
                .thenReturn(
                        memberAccess("member-1")
                );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        ChangeTaskStatusRequest request =
                new ChangeTaskStatusRequest(
                        TaskStatus.IN_PROGRESS
                );

        taskService.changeTaskStatus(
                "task-1",
                request,
                "member-1"
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );

        verify(
                taskRepository
        ).save(task);
    }

    // Test for publishing TaskStatusChangedEvent when a task's status changes from TODO to IN_PROGRESS...
    @Test
    void statusChangeShouldPublishStatusChangedEvent() {

        Task task =
                task(TaskStatus.TODO);

        when(taskRepository.findById("task-1"))
                .thenReturn(Optional.of(task));

        when(projectServiceClient.getProjectAccess("project-1"))
                .thenReturn(
                        memberAccess("member-1")
                );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        taskService.changeTaskStatus(
                "task-1",
                new ChangeTaskStatusRequest(
                        TaskStatus.IN_PROGRESS
                ),
                "member-1"
        );

        ArgumentCaptor<TaskStatusChangedEvent> captor =
                ArgumentCaptor.forClass(
                        TaskStatusChangedEvent.class
                );

        verify(eventPublisher)
                .publishEvent(
                        captor.capture()
                );

        TaskStatusChangedEvent event =
                captor.getValue();

        assertEquals(
                "task-1",
                event.taskId()
        );

        assertEquals(
                TaskStatus.TODO,
                event.previousStatus()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                event.newStatus()
        );

        assertEquals(
                "member-1",
                event.changedBy()
        );
    }

    // Test for completing a task and publishing the corresponding events...
    @Test
    void completingTaskShouldSetCompletedAtAndPublishCompletedEvent() {

        Task task =
                task(TaskStatus.IN_REVIEW);

        when(taskRepository.findById("task-1"))
                .thenReturn(Optional.of(task));

        when(projectServiceClient.getProjectAccess("project-1"))
                .thenReturn(
                        memberAccess("member-1")
                );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        taskService.changeTaskStatus(
                "task-1",
                new ChangeTaskStatusRequest(
                        TaskStatus.COMPLETED
                ),
                "member-1"
        );

        assertEquals(
                TaskStatus.COMPLETED,
                task.getStatus()
        );

        assertNotNull(
                task.getCompletedAt()
        );

        verify(eventPublisher)
                .publishEvent(
                        any(TaskStatusChangedEvent.class)
                );

        verify(eventPublisher)
                .publishEvent(
                        any(TaskCompletedEvent.class)
                );
    }

    // Test for reopening a completed task by a manager and clearing the completedAt timestamp...
    @Test
    void managerReopeningCompletedTaskShouldClearCompletedAt() {

        Task task =
                task(TaskStatus.COMPLETED);

        task.setCompletedAt(
                java.time.Instant.now()
        );

        when(taskRepository.findById("task-1"))
                .thenReturn(Optional.of(task));

        when(projectServiceClient.getProjectAccess("project-1"))
                .thenReturn(
                        managerAccess("manager-1")
                );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        taskService.changeTaskStatus(
                "task-1",
                new ChangeTaskStatusRequest(
                        TaskStatus.IN_PROGRESS
                ),
                "manager-1"
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                task.getStatus()
        );

        assertNull(
                task.getCompletedAt()
        );
    }
}