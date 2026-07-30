package edu.bbte.daygig.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.bbte.daygig.backend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    boolean existsByEventId(UUID eventId);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    List<Notification> findTop10ByRecipientIdAndReadFalseOrderByCreatedAtDesc(UUID recipientId);

    Optional<Notification> findByIdAndRecipientId(UUID notificationId, UUID recipientId);

    @Modifying
    @Query("""
            UPDATE Notification notification
            SET notification.read = true
            WHERE notification.recipient.id = :recipientId
              AND notification.read = false
            """)
    int markAllAsRead(@Param("recipientId") UUID recipientId);

    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}
