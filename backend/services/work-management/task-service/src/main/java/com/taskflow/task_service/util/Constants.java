package com.taskflow.task_service.util;

public final class Constants {

    private Constants() {
    }

    public static final String API_V1 = "/api/v1";

    public static final String TASKS_PATH = API_V1 + "/tasks";

    public static final String ADMIN_TASKS_PATH =
            API_V1 + "/admin/tasks";

    public static final String AUTHORIZATION_HEADER =
            "Authorization";

    public static final String BEARER_PREFIX =
            "Bearer ";

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    public static final int MAX_TASK_TITLE_LENGTH = 200;

    public static final int MAX_TASK_DESCRIPTION_LENGTH = 5000;

    public static final int MAX_COMMENT_LENGTH = 5000;

    public static final String DEFAULT_SORT_FIELD =
            "createdAt";
}