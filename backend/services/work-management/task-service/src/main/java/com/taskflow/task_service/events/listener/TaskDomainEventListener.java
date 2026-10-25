package com.taskflow.task_service.events.listener;

import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;
import com.taskflow.task_service.events.publisher.TaskEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges Spring domain events to an optional external TaskEventPublisher.
 * AFTER_COMMIT runs after an active transaction commits; fallbackExecution also handles calls
 * without a transaction. Missing publishers are explicitly skipped for isolated contexts/tests.
 * The bridge plus async Kafka callbacks is best effort, not a durable outbox or exactly-once
 * transaction spanning MongoDB and Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskDomainEventListener {

    private final ObjectProvider<TaskEventPublisher> taskEventPublisherProvider;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handleTaskStatusChanged(
            TaskStatusChangedEvent event
    ) {

        log.debug(
                "Handling task status changed event: taskId={}, {} -> {}",
                event.taskId(),
                event.previousStatus(),
                event.newStatus()
        );

        TaskEventPublisher publisher =
                taskEventPublisherProvider.getIfAvailable();

        if (publisher == null) {

            log.debug(
                    "No TaskEventPublisher configured. "
                            + "Skipping external publication for taskId={}",
                    event.taskId()
            );

            return;
        }

        publisher.publishTaskStatusChanged(event);
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handleTaskCompleted(
            TaskCompletedEvent event
    ) {

        log.debug(
                "Handling task completed event: taskId={}",
                event.taskId()
        );

        TaskEventPublisher publisher =
                taskEventPublisherProvider.getIfAvailable();

        if (publisher == null) {

            log.debug(
                    "No TaskEventPublisher configured. "
                            + "Skipping external publication for taskId={}",
                    event.taskId()
            );

            return;
        }

        publisher.publishTaskCompleted(event);
    }
}
