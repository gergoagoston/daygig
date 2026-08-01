package edu.bbte.daygig.backend.messaging.rabbit.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DomainEventEnvelope<T>(
        UUID eventId,
        String eventType,
        UUID recipientUserId,
        T payload,
        Instant occurredAt
) {

    public DomainEventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(recipientUserId, "recipientUserId must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
    }

    public static <T> DomainEventEnvelope<T> create(
            String eventType,
            UUID recipientUserId,
            T payload
    ) {
        return new DomainEventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                recipientUserId,
                payload,
                Instant.now()
        );
    }
}
