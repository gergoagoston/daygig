package edu.bbte.daygig.backend.messaging.event;

import java.util.UUID;

public record JobActivityEventPayload(
        UUID jobId,
        UUID applicationId
) {
}
