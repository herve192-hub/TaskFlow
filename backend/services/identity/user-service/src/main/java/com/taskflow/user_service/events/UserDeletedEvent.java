package com.taskflow.user_service.events;

import java.time.Instant;

/**
 * Candidate profile-deactivation event keyed by canonical authUserId and deactivation time.
 * The current service changes status to INACTIVE but does not publish this record or delete
 * the credential account. Consumers must not assume a deletion cascade exists.
 */
public record UserDeletedEvent(
        String authUserId,
        Instant deactivatedAt
) {
}
