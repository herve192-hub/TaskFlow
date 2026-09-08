package com.taskflow.task_service.events.listener;

import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;
import com.taskflow.task_service.events.publisher.TaskEventPublisher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;

import static org.mockito.Mockito.*;

class TaskDomainEventListenerTest {

    private TaskEventPublisher taskEventPublisher;

    private ObjectProvider<TaskEventPublisher> publisherProvider;

    private TaskDomainEventListener listener;

    @BeforeEach
    void setUp() {

        taskEventPublisher =
                mock(TaskEventPublisher.class);

        publisherProvider =
                mock(ObjectProvider.class);

        when(publisherProvider.getIfAvailable())
                .thenReturn(taskEventPublisher);

        listener =
                new TaskDomainEventListener(
                        publisherProvider
                );
    }

    @Test
    void shouldPublishTaskStatusChangedEvent() {

        TaskStatusChangedEvent event =
                new TaskStatusChangedEvent(
                        "task-1",
                        "project-1",
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        "user-1",
                        Instant.now()
                );

        listener.handleTaskStatusChanged(event);

        verify(taskEventPublisher)
                .publishTaskStatusChanged(event);
    }

    @Test
    void shouldPublishTaskCompletedEvent() {

        Instant completedAt =
                Instant.now();

        TaskCompletedEvent event =
                new TaskCompletedEvent(
                        "task-1",
                        "project-1",
                        "user-1",
                        "assignee-1",
                        8.0,
                        7.5,
                        completedAt
                );

        listener.handleTaskCompleted(event);

        verify(taskEventPublisher)
                .publishTaskCompleted(event);
    }

    @Test
    void shouldNotFailWhenPublisherIsNotConfigured() {

        when(publisherProvider.getIfAvailable())
                .thenReturn(null);

        TaskStatusChangedEvent event =
                new TaskStatusChangedEvent(
                        "task-1",
                        "project-1",
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        "user-1",
                        Instant.now()
                );

        listener.handleTaskStatusChanged(event);

        verifyNoInteractions(taskEventPublisher);
    }
}