package edu.bbte.daygig.backend.dto.notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationOutDto(
        UUID id,
        String type,
        UUID jobId,
        LocalDateTime createdAt
) {
}
