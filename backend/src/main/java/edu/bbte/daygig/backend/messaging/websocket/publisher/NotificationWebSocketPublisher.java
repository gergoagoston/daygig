package edu.bbte.daygig.backend.messaging.websocket.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import edu.bbte.daygig.backend.notification.event.NotificationCreatedEvent;

@Component
public class NotificationWebSocketPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWebSocketPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(NotificationCreatedEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    event.recipientUserId().toString(),
                    "/exchange/amq.direct/notifications",
                    event.notification()
            );
        } catch (MessagingException exception) {
            LOG.warn(
                    "Could not deliver notification over WebSocket. notificationId={}",
                    event.notification().id(),
                    exception
            );
        }
    }
}
