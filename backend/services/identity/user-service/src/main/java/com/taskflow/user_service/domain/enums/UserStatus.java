package com.taskflow.user_service.domain.enums;

/**
 * Profile lifecycle labels used in persistence and directory responses.
 * searchUsers restricts search to ACTIVE, while batch lookup intentionally preserves existing
 * inactive membership references. Setting INACTIVE deactivates a profile, not the separate auth
 * account or already issued access tokens.
 */
public enum UserStatus {

    ACTIVE,

    INACTIVE,

    PENDING,

    SUSPENDED
}
