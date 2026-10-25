package com.taskflow.user_service.events;

import java.time.Instant;

/**
 * Immutable candidate profile-created event shape carrying canonical account/display metadata.
 * The current UserServiceImpl does not publish this record or provide a broker adapter.
 * A future publisher must decide delivery guarantees and data-retention scope explicitly.
 */
public record UserCreatedEvent(
        String authUserId,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}
