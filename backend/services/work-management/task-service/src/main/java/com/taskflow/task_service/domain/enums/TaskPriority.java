package com.taskflow.task_service.domain.enums;


/**
 * Task urgency labels serialized in persistence, API DTOs, and frontend badges/forms.
 * Priority is independent of lifecycle status and permissions; CRITICAL is a label, not an
 * escalation workflow or an administrator authority.
 */
public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
