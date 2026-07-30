package edu.bbte.daygig.backend.service.notification;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.notification.NotificationSummaryOutDto;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.mappers.NotificationMapper;
import edu.bbte.daygig.backend.model.Notification;
import edu.bbte.daygig.backend.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationSummaryOutDto getUnread(UUID recipientId) {
        return new NotificationSummaryOutDto(
                notificationRepository.countByRecipientIdAndReadFalse(recipientId),
                notificationRepository
                        .findTop10ByRecipientIdAndReadFalseOrderByCreatedAtDesc(recipientId)
                        .stream()
                        .map(notificationMapper::toDto)
                        .toList()
        );
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId,
                           UUID recipientId) {
        Notification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId
                ));
        notification.setRead(true);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllAsRead(recipientId);
    }
}
