package com.taskflow.task_service.events.publisher;

import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;

/** Transport boundary for task domain events; Kafka is the current external implementation. */
public interface TaskEventPublisher {

    void publishTaskStatusChanged(
            TaskStatusChangedEvent event
    );

    void publishTaskCompleted(
            TaskCompletedEvent event
    );
}
