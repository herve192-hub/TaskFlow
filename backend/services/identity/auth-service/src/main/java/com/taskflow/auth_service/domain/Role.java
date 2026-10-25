package com.taskflow.auth_service.domain;

/**
 * Platform authorities serialized into access tokens and converted to Spring Security roles.
 * ROLE_ADMIN controls account provisioning in production; ROLE_USER is assigned to normal accounts.
 * These values are separate from project-service's OWNER/ADMIN/MANAGER membership roles.
 */
public enum Role {
    ROLE_USER,

    ROLE_ADMIN

    // ROLE_MANAGER
    // ROLE_SUPER_ADMIN
}
