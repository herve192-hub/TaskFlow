package com.taskflow.user_service.util;

/**
 * Optional shared route-fragment constants for profile-module callers.
 * Defining a path here does not register a controller endpoint. Keep any new use consistent
 * with UserController mappings and the API gateway's explicit public route allowlist.
 */
public final class Constants {

     private Constants() {}

    public static final String API_V1="/api/v1";

    public static final String USERS="/users";

    public static final String ADMIN="/admin";

    
}
