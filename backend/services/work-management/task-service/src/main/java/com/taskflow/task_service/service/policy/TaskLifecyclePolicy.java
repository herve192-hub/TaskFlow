package com.taskflow.task_service.service.policy;

import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.exception.InvalidTaskException;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TaskLifecyclePolicy {

    private final Map<TaskStatus, Set<TaskStatus>>
            allowedTransitions;

    public TaskLifecyclePolicy() {

        allowedTransitions =
                new EnumMap<>(TaskStatus.class);

        /*
         * New task.
         *
         * It may start being worked,
         * become blocked before work begins,
         * or be cancelled by project management...
         */
        allowedTransitions.put(
                TaskStatus.TODO,
                EnumSet.of(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.BLOCKED,
                        TaskStatus.CANCELLED
                )
        );

        /*
         * Active work...
         */
        allowedTransitions.put(
                TaskStatus.IN_PROGRESS,
                EnumSet.of(
                        TaskStatus.TODO,
                        TaskStatus.IN_REVIEW,
                        TaskStatus.BLOCKED,
                        TaskStatus.CANCELLED
                )
        );

        /*
         * Work waiting for review...
         */
        allowedTransitions.put(
                TaskStatus.IN_REVIEW,
                EnumSet.of(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.BLOCKED,
                        TaskStatus.COMPLETED,
                        TaskStatus.CANCELLED
                )
        );

        /*
         * Blocked work may return to TODO
         * or resume directly...
         */
        allowedTransitions.put(
                TaskStatus.BLOCKED,
                EnumSet.of(
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.CANCELLED
                )
        );

        /*
         * Completed work may be reopened...
         */
        allowedTransitions.put(
                TaskStatus.COMPLETED,
                EnumSet.of(
                        TaskStatus.IN_PROGRESS
                )
        );

        /*
         * Cancelled work may be restored...
         */
        allowedTransitions.put(
                TaskStatus.CANCELLED,
                EnumSet.of(
                        TaskStatus.TODO
                )
        );
    }

    public void validateTransition(
            TaskStatus currentStatus,
            TaskStatus newStatus
    ) {

        if (currentStatus == null) {
            throw new InvalidTaskException(
                    "Current task status cannot be null"
            );
        }

        if (newStatus == null) {
            throw new InvalidTaskException(
                    "New task status cannot be null"
            );
        }

        if (currentStatus == newStatus) {
            throw new InvalidTaskException(
                    "Task is already in status "
                            + newStatus
            );
        }

        Set<TaskStatus> allowed =
                allowedTransitions.getOrDefault(
                        currentStatus,
                        Set.of()
                );

        if (!allowed.contains(newStatus)) {

            throw new InvalidTaskException(
                    "Invalid task status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }
    }
}