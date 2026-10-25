package com.taskflow.user_service.events;

import java.time.Instant;

/**
 * Immutable candidate profile-update event describing selected display fields and occurrence time.
 * This record is not automatically emitted by repository saves; current update flows persist
 * through UserServiceImpl without wiring an event publisher.
 */
public record UserUpdatedEvent(
        String authUserId,
        String firstName,
        String lastName,
        String avatarUrl,
        Instant updatedAt
) {
}
