package com.taskflow.user_service.events;

import java.time.Instant;

public record UserCreatedEvent(
        String authUserId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}