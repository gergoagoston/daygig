package edu.bbte.daygig.backend.messaging.rabbit.consumer;

import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import edu.bbte.daygig.backend.mappers.NotificationMapper;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.repository.NotificationRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldSkipAnotherUnreadNotificationForTheSameConversation() {
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        DomainEventEnvelope<JobActivityEventPayload> event = DomainEventEnvelope.create(
                RabbitNames.MESSAGE_CREATED,
                recipientId,
                JobActivityEventPayload.forChat(
                        UUID.randomUUID(),
                        conversationId,
                        UUID.randomUUID()
                )
        );
        when(notificationRepository.existsByEventId(event.eventId())).thenReturn(false);
        when(notificationRepository.existsByRecipientIdAndConversationIdAndReadFalse(
                recipientId,
                conversationId
        )).thenReturn(true);
        NotificationEventConsumer consumer = new NotificationEventConsumer(
                notificationRepository,
                userRepository,
                notificationMapper,
                eventPublisher
        );

        consumer.consume(event);

        verify(notificationRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(userRepository, notificationMapper, eventPublisher);
    }
}
