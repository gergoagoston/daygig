package edu.bbte.daygig.backend.service.notification;

import java.util.UUID;
import edu.bbte.daygig.backend.dto.notification.NotificationSummaryOutDto;

public interface NotificationService {

    NotificationSummaryOutDto getUnread(UUID recipientId);

    void markAsRead(UUID notificationId, UUID recipientId);

    void markAllAsRead(UUID recipientId);
}
