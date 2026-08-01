package edu.bbte.daygig.backend.messaging.rabbit.event;

import java.util.UUID;

public record JobActivityEventPayload(
        UUID jobId,
        UUID applicationId
) {
}
