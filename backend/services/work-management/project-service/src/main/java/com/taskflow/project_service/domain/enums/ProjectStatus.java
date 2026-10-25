package com.taskflow.project_service.domain.enums;

/**
 * Stored project lifecycle labels used in request/response DTOs and UI badges.
 * This enum alone implements no transition policy and does not alter task lifecycle state.
 * Current service authorization is membership-based regardless of the label.
 */
public enum ProjectStatus {
    
    PLANNING,
    
    // IN_PROGRESS,
    
    ACTIVE,
    
    ON_HOLD,
    
    COMPLETED,
    
    ARCHIVED
}
