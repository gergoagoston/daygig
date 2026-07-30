package edu.bbte.daygig.backend.service.notification;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRepository notificationRepository;

    @Value("${app.notifications.retention-days:30}")
    private long retentionDays;

    @Scheduled(cron = "${app.notifications.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void deleteExpiredNotifications() {
        notificationRepository.deleteByCreatedAtBefore(
                LocalDateTime.now().minusDays(retentionDays)
        );
    }
}
