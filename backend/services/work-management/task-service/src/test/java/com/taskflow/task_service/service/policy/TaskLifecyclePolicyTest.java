package com.taskflow.task_service.service.policy;

import com.taskflow.task_service.domain.enums.TaskStatus;
import com.taskflow.task_service.exception.InvalidTaskException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskLifecyclePolicyTest {

    private TaskLifecyclePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new TaskLifecyclePolicy();
    }

    @Test
    void todoToInProgressShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void todoToBlockedShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.BLOCKED
                )
        );
    }

    @Test
    void todoToCancelledShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.CANCELLED
                )
        );
    }

    @Test
    void todoToCompletedShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.COMPLETED
                )
        );
    }

    @Test
    void todoToInReviewShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.IN_REVIEW
                )
        );
    }

    @Test
    void inProgressToInReviewShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.IN_REVIEW
                )
        );
    }

    @Test
    void inProgressToBlockedShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.BLOCKED
                )
        );
    }

    @Test
    void inProgressToTodoShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void inProgressToCompletedShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.IN_PROGRESS,
                        TaskStatus.COMPLETED
                )
        );
    }

    @Test
    void inReviewToCompletedShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.IN_REVIEW,
                        TaskStatus.COMPLETED
                )
        );
    }

    @Test
    void inReviewToInProgressShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.IN_REVIEW,
                        TaskStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void blockedToInProgressShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.BLOCKED,
                        TaskStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void blockedToTodoShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.BLOCKED,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void blockedToCompletedShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.BLOCKED,
                        TaskStatus.COMPLETED
                )
        );
    }

    @Test
    void completedToInProgressShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.COMPLETED,
                        TaskStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void completedToTodoShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.COMPLETED,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void cancelledToTodoShouldBeAllowed() {

        assertDoesNotThrow(() ->
                policy.validateTransition(
                        TaskStatus.CANCELLED,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void cancelledToInProgressShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.CANCELLED,
                        TaskStatus.IN_PROGRESS
                )
        );
    }

    @Test
    void sameStatusTransitionShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.TODO,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void nullCurrentStatusShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        null,
                        TaskStatus.TODO
                )
        );
    }

    @Test
    void nullNewStatusShouldBeRejected() {

        assertThrows(
                InvalidTaskException.class,
                () -> policy.validateTransition(
                        TaskStatus.TODO,
                        null
                )
        );
    }
}