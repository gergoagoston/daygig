package edu.bbte.daygig.backend.controller;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.notification.NotificationSummaryOutDto;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.notification.NotificationService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationSummaryOutDto getUnread(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return notificationService.getUnread(currentUserId(principal));
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        notificationService.markAsRead(notificationId, currentUserId(principal));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        notificationService.markAllAsRead(currentUserId(principal));
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
