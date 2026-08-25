package com.taskflow.user_service.events;

import java.time.Instant;

public record UserDeletedEvent(
        String authUserId,
        Instant deactivatedAt
) {
}