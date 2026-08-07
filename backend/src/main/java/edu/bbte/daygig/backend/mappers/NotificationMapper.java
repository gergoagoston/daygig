package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.notification.NotificationOutDto;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.model.Notification;
import edu.bbte.daygig.backend.model.User;

@Component
public class NotificationMapper {

    public Notification fromEvent(
            DomainEventEnvelope<JobActivityEventPayload> event,
            User recipient
    ) {
        Notification notification = new Notification();
        notification.setEventId(event.eventId());
        notification.setRecipient(recipient);
        notification.setType(event.eventType());
        notification.setJobId(event.payload().jobId());
        notification.setConversationId(event.payload().conversationId());
        notification.setRead(false);
        return notification;
    }

    public NotificationOutDto toDto(Notification notification) {
        return new NotificationOutDto(
                notification.getId(),
                notification.getType(),
                notification.getJobId(),
                notification.getConversationId(),
                notification.getCreatedAt()
        );
    }
}
