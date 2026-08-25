package com.taskflow.user_service.events;

import java.time.Instant;

public record UserUpdatedEvent(
        String authUserId,
        String firstName,
        String lastName,
        String avatarUrl,
        Instant updatedAt
) {
}