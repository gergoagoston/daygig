package edu.bbte.daygig.backend.dto.notification;

import java.util.List;

public record NotificationSummaryOutDto(
        long unreadCount,
        List<NotificationOutDto> notifications
) {
}
