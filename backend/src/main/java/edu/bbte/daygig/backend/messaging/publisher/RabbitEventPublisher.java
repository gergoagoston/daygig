package edu.bbte.daygig.backend.messaging.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import edu.bbte.daygig.backend.messaging.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.event.DomainEventEnvelope;

@Component
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RabbitEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(DomainEventEnvelope<?> event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitNames.EVENTS_EXCHANGE,
                    event.eventType(),
                    event
            );
        } catch (AmqpException exception) {
            LOG.error(
                    "Could not publish committed domain event. eventId={}, eventType={}",
                    event.eventId(),
                    event.eventType(),
                    exception
            );
        }
    }
}
