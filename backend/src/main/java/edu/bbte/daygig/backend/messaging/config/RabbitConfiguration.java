package edu.bbte.daygig.backend.messaging.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RabbitConfiguration {

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Declarables rabbitDeclarables() {
        DirectExchange exchange = new DirectExchange(
                RabbitNames.EVENTS_EXCHANGE,
                true,
                false
        );
        Queue notificationQueue = QueueBuilder
                .durable(RabbitNames.NOTIFICATION_QUEUE)
                .build();
        Queue emailQueue = QueueBuilder
                .durable(RabbitNames.EMAIL_QUEUE)
                .build();

        return new Declarables(
                exchange,
                notificationQueue,
                emailQueue,
                BindingBuilder
                        .bind(notificationQueue)
                        .to(exchange)
                        .with(RabbitNames.APPLICATION_CREATED),
                BindingBuilder
                        .bind(notificationQueue)
                        .to(exchange)
                        .with(RabbitNames.APPLICATION_REJECTED),
                BindingBuilder
                        .bind(notificationQueue)
                        .to(exchange)
                        .with(RabbitNames.APPLICATION_WITHDRAWN),
                BindingBuilder
                        .bind(notificationQueue)
                        .to(exchange)
                        .with(RabbitNames.JOB_COMPLETED),
                BindingBuilder
                        .bind(emailQueue)
                        .to(exchange)
                        .with(RabbitNames.APPLICATION_REJECTED)
        );
    }
}
