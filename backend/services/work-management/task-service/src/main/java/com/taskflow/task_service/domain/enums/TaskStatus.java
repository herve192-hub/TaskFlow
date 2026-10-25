package com.taskflow.task_service.domain.enums;

/**
 * Task workflow states shared by persistence, API requests, frontend controls, and event schemas.
 * TaskLifecyclePolicy defines valid edges and TaskServiceImpl applies caller-specific restrictions.
 * Changing a name requires coordinating stored data, clients, tests, and Kafka message compatibility.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    BLOCKED,
    COMPLETED,
    CANCELLED
}
