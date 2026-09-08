package com.taskflow.task_service.events.publisher.kafka;

import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;
import com.taskflow.task_service.events.publisher.kafka.message.TaskCompletedMessage;
import com.taskflow.task_service.events.publisher.kafka.message.TaskStatusChangedMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaTaskEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaTaskEventPublisher publisher;

    @BeforeEach
    void setUp() {

        publisher =
                new KafkaTaskEventPublisher(
                        kafkaTemplate,
                        "task.status.changed.v1",
                        "task.completed.v1"
                );
    }

    @Test
    void shouldPublishTaskStatusChangedMessage() {

        TaskStatusChangedEvent event =
                new TaskStatusChangedEvent(
                        "task-1",
                        "project-1",
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        "user-1",
                        Instant.now()
                );

        when(
                kafkaTemplate.send(
                        eq("task.status.changed.v1"),
                        eq("task-1"),
                        any()
                )
        ).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        publisher.publishTaskStatusChanged(event);

        ArgumentCaptor<Object> messageCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(
                eq("task.status.changed.v1"),
                eq("task-1"),
                messageCaptor.capture()
        );

        assertInstanceOf(
                TaskStatusChangedMessage.class,
                messageCaptor.getValue()
        );

        TaskStatusChangedMessage message =
                (TaskStatusChangedMessage)
                        messageCaptor.getValue();

        assertNotNull(message.eventId());

        assertEquals(
                "task.status.changed",
                message.eventType()
        );

        assertEquals(
                1,
                message.schemaVersion()
        );

        assertEquals(
                "task-1",
                message.taskId()
        );

        assertEquals(
                "project-1",
                message.projectId()
        );

        assertEquals(
                TaskStatus.TODO,
                message.previousStatus()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                message.newStatus()
        );

        assertEquals(
                "user-1",
                message.changedBy()
        );
    }

    @Test
    void shouldPublishTaskCompletedMessage() {

        Instant completedAt =
                Instant.now();

        TaskCompletedEvent event =
                new TaskCompletedEvent(
                        "task-1",
                        "project-1",
                        "user-1",
                        "assignee-1",
                        8.0,
                        6.5,
                        completedAt
                );

        when(
                kafkaTemplate.send(
                        eq("task.completed.v1"),
                        eq("task-1"),
                        any()
                )
        ).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        publisher.publishTaskCompleted(event);

        ArgumentCaptor<Object> messageCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(
                eq("task.completed.v1"),
                eq("task-1"),
                messageCaptor.capture()
        );

        assertInstanceOf(
                TaskCompletedMessage.class,
                messageCaptor.getValue()
        );

        TaskCompletedMessage message =
                (TaskCompletedMessage)
                        messageCaptor.getValue();

        assertNotNull(message.eventId());

        assertEquals(
                "task.completed",
                message.eventType()
        );

        assertEquals(
                1,
                message.schemaVersion()
        );

        assertEquals(
                "task-1",
                message.taskId()
        );

        assertEquals(
                "assignee-1",
                message.assigneeId()
        );

        assertEquals(
                completedAt,
                message.occurredAt()
        );
    }

    @Test
    void shouldUseTaskIdAsKafkaKey() {

        TaskStatusChangedEvent event =
                new TaskStatusChangedEvent(
                        "task-123",
                        "project-1",
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.IN_REVIEW,
                        "user-1",
                        Instant.now()
                );

        when(
                kafkaTemplate.send(
                        anyString(),
                        anyString(),
                        any()
                )
        ).thenReturn(
                CompletableFuture.completedFuture(null)
        );

        publisher.publishTaskStatusChanged(event);

        verify(kafkaTemplate).send(
                eq("task.status.changed.v1"),
                eq("task-123"),
                any(TaskStatusChangedMessage.class)
        );
    }
}