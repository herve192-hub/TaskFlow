package com.taskflow.task_service.events.publisher.kafka;

import com.taskflow.task_service.events.TaskCompletedEvent;
import com.taskflow.task_service.events.TaskStatusChangedEvent;
import com.taskflow.task_service.events.publisher.TaskEventPublisher;
import com.taskflow.task_service.events.publisher.kafka.message.TaskCompletedMessage;
import com.taskflow.task_service.events.publisher.kafka.message.TaskStatusChangedMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Serializes domain events to versioned messages and sends them through KafkaTemplate.
 * Topic names come from taskflow.kafka.topics.* configuration and taskId is the partition key,
 * preserving per-task ordering within Kafka's partition semantics. Async send failures are logged
 * rather than rolled back into the HTTP operation; no persistent outbox/replay mechanism exists.
 */
@Slf4j
@Component
public class KafkaTaskEventPublisher
        implements TaskEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String taskStatusChangedTopic;
    private final String taskCompletedTopic;

    public KafkaTaskEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,

            @Value("${taskflow.kafka.topics.task-status-changed}")
            String taskStatusChangedTopic,

            @Value("${taskflow.kafka.topics.task-completed}")
            String taskCompletedTopic
    ) {

        this.kafkaTemplate = kafkaTemplate;
        this.taskStatusChangedTopic =
                taskStatusChangedTopic;
        this.taskCompletedTopic =
                taskCompletedTopic;
    }

    @Override
    public void publishTaskStatusChanged(
            TaskStatusChangedEvent event
    ) {

        TaskStatusChangedMessage message =
                TaskStatusChangedMessage.from(event);

        kafkaTemplate.send(
                taskStatusChangedTopic,
                event.taskId(),
                message
        ).whenComplete(
                (result, exception) -> {

                    if (exception != null) {

                        log.error(
                                "Failed to publish task status changed event: "
                                        + "eventId={}, taskId={}, topic={}",
                                message.eventId(),
                                event.taskId(),
                                taskStatusChangedTopic,
                                exception
                        );

                        return;
                    }

                    log.debug(
                            "Published task status changed event: "
                                    + "eventId={}, taskId={}, topic={}",
                            message.eventId(),
                            event.taskId(),
                            taskStatusChangedTopic
                    );
                }
        );
    }

    @Override
    public void publishTaskCompleted(
            TaskCompletedEvent event
    ) {

        TaskCompletedMessage message =
                TaskCompletedMessage.from(event);

        kafkaTemplate.send(
                taskCompletedTopic,
                event.taskId(),
                message
        ).whenComplete(
                (result, exception) -> {

                    if (exception != null) {

                        log.error(
                                "Failed to publish task completed event: "
                                        + "eventId={}, taskId={}, topic={}",
                                message.eventId(),
                                event.taskId(),
                                taskCompletedTopic,
                                exception
                        );

                        return;
                    }

                    log.debug(
                            "Published task completed event: "
                                    + "eventId={}, taskId={}, topic={}",
                            message.eventId(),
                            event.taskId(),
                            taskCompletedTopic
                    );
                }
        );
    }
}
