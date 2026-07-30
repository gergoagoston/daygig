package edu.bbte.daygig.backend.messaging.event;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventEnvelopeTest {

    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Test
    void shouldCreateEnvelopeWithGeneratedMetadata() {
        Map<String, String> payload = Map.of("applicationId", "application-1");

        DomainEventEnvelope<Map<String, String>> event = DomainEventEnvelope.create(
                "application.created",
                RECIPIENT_ID,
                payload
        );

        assertNotNull(event.eventId());
        assertEquals("application.created", event.eventType());
        assertEquals(RECIPIENT_ID, event.recipientUserId());
        assertEquals(payload, event.payload());
        assertNotNull(event.occurredAt());
    }

    @Test
    void shouldGenerateUniqueEventIds() {
        DomainEventEnvelope<String> first = DomainEventEnvelope.create(
                "application.created",
                RECIPIENT_ID,
                "payload"
        );
        DomainEventEnvelope<String> second = DomainEventEnvelope.create(
                "application.created",
                RECIPIENT_ID,
                "payload"
        );

        assertNotEquals(first.eventId(), second.eventId());
    }

    @Test
    void shouldRejectBlankEventType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DomainEventEnvelope.create(" ", RECIPIENT_ID, "payload")
        );
    }
}
