package com.taskflow.task_service.events.publisher;

import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;

/**
 * Transport-independent port for status-change and completion publication.
 * TaskDomainEventListener invokes it and KafkaTaskEventPublisher supplies the current adapter.
 * Adding another domain record does not automatically add publication support to this contract.
 */
public interface TaskEventPublisher {

    void publishTaskStatusChanged(
            TaskStatusChangedEvent event
    );

    void publishTaskCompleted(
            TaskCompletedEvent event
    );
}
