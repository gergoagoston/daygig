package edu.bbte.daygig.backend.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.messaging.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.mappers.NotificationMapper;
import edu.bbte.daygig.backend.model.Notification;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.NotificationRepository;
import edu.bbte.daygig.backend.repository.UserRepository;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @RabbitListener(queues = RabbitNames.NOTIFICATION_QUEUE)
    @Transactional
    public void consume(DomainEventEnvelope<JobActivityEventPayload> event) {
        if (notificationRepository.existsByEventId(event.eventId())) {
            return;
        }

        User recipient = userRepository.getReferenceById(event.recipientUserId());
        Notification notification = notificationMapper.fromEvent(event, recipient);

        notificationRepository.save(notification);
    }
}
