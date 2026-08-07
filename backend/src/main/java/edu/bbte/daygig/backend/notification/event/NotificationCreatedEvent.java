package edu.bbte.daygig.backend.notification.event;

import java.util.UUID;
import edu.bbte.daygig.backend.dto.notification.NotificationOutDto;

public record NotificationCreatedEvent(
        UUID recipientUserId,
        NotificationOutDto notification
) {
}
