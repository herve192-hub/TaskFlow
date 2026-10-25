/**
 * Reserved ProjectCreatedEvent source placeholder with no event type or publication behavior.
 * ProjectServiceImpl currently persists the related changes directly; it does not emit this
 * file through Kafka or Spring events. Introducing events requires an explicit schema and
 * failure/delivery policy, especially across project and task service boundaries.
 */
