package edu.bbte.daygig.backend.messaging.rabbit.event;

import java.util.UUID;

public record JobActivityEventPayload(
        UUID jobId,
        UUID applicationId,
        UUID conversationId,
        UUID senderUserId
) {

    public JobActivityEventPayload(UUID jobId,
                                   UUID applicationId) {
        this(jobId, applicationId, null, null);
    }

    public static JobActivityEventPayload forChat(UUID jobId,
                                                  UUID conversationId,
                                                  UUID senderUserId) {
        return new JobActivityEventPayload(
                jobId,
                null,
                conversationId,
                senderUserId
        );
    }
}
