package com.taskflow.task_service.domain.enums;

/**
 * Classification labels for tasks, bugs, features, improvements, and epics.
 * These labels do not change permission checks or establish parent/child relationships;
 * parentTaskId is separate task metadata and lifecycle remains governed by TaskStatus.
 */
public enum TaskType {

    TASK,
    BUG,
    FEATURE,
    IMPROVEMENT,
    EPIC
}
