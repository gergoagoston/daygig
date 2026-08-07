package edu.bbte.daygig.backend.messaging.rabbit.publisher;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitEventPublisher eventPublisher;

    @Test
    void shouldUseEventTypeAsRoutingKey() {
        DomainEventEnvelope<Map<String, UUID>> event = DomainEventEnvelope.create(
                RabbitNames.APPLICATION_CREATED,
                UUID.randomUUID(),
                Map.of("applicationId", UUID.randomUUID())
        );

        eventPublisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitNames.EVENTS_EXCHANGE,
                RabbitNames.APPLICATION_CREATED,
                event
        );
    }

    @Test
    void shouldNotFailCommittedOperationWhenRabbitMqIsUnavailable() {
        DomainEventEnvelope<String> event = DomainEventEnvelope.create(
                RabbitNames.APPLICATION_CREATED,
                UUID.randomUUID(),
                "payload"
        );
        doThrow(new AmqpConnectException(new RuntimeException("RabbitMQ unavailable")))
                .when(rabbitTemplate)
                .convertAndSend(
                        RabbitNames.EVENTS_EXCHANGE,
                        RabbitNames.APPLICATION_CREATED,
                        event
                );

        assertDoesNotThrow(() -> eventPublisher.publish(event));
    }
}
