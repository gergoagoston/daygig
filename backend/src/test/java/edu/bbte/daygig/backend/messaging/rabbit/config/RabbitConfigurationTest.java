package edu.bbte.daygig.backend.messaging.rabbit.config;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RabbitConfigurationTest {

    private Collection<Declarable> declarables;
    private DirectExchange exchange;
    private Queue notificationQueue;
    private Queue emailQueue;

    @BeforeEach
    void setUp() {
        Declarables topology = new RabbitConfiguration().rabbitDeclarables();
        declarables = topology.getDeclarables();
        exchange = findExchange();
        notificationQueue = findQueue(RabbitNames.NOTIFICATION_QUEUE);
        emailQueue = findQueue(RabbitNames.EMAIL_QUEUE);
    }

    @Test
    void shouldDeclareDurableEventExchange() {
        assertEquals(11, declarables.size());
        assertEquals(RabbitNames.EVENTS_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void shouldDeclareDurableQueues() {
        assertEquals(RabbitNames.NOTIFICATION_QUEUE, notificationQueue.getName());
        assertTrue(notificationQueue.isDurable());
        assertFalse(notificationQueue.isExclusive());
        assertFalse(notificationQueue.isAutoDelete());

        assertEquals(RabbitNames.EMAIL_QUEUE, emailQueue.getName());
        assertTrue(emailQueue.isDurable());
        assertFalse(emailQueue.isExclusive());
        assertFalse(emailQueue.isAutoDelete());
    }

    @Test
    void shouldBindNotificationEvents() {
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.APPLICATION_CREATED
        );
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.APPLICATION_REJECTED
        );
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.APPLICATION_WITHDRAWN
        );
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.JOB_COMPLETED
        );
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.CONVERSATION_STARTED
        );
        assertBinding(
                RabbitNames.NOTIFICATION_QUEUE,
                RabbitNames.MESSAGE_CREATED
        );
    }

    @Test
    void shouldBindRejectedApplicationEventToEmailQueue() {
        assertBinding(
                RabbitNames.EMAIL_QUEUE,
                RabbitNames.APPLICATION_REJECTED
        );
        assertBinding(
                RabbitNames.EMAIL_QUEUE,
                RabbitNames.CONVERSATION_STARTED
        );
    }

    private DirectExchange findExchange() {
        return declarables
                .stream()
                .filter(DirectExchange.class::isInstance)
                .map(DirectExchange.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private Queue findQueue(String queueName) {
        return declarables
                .stream()
                .filter(Queue.class::isInstance)
                .map(Queue.class::cast)
                .filter(queue -> queueName.equals(queue.getName()))
                .findFirst()
                .orElseThrow();
    }

    private void assertBinding(String queueName,
                               String routingKey) {
        Binding binding = declarables
                .stream()
                .filter(Binding.class::isInstance)
                .map(Binding.class::cast)
                .filter(candidate -> queueName.equals(candidate.getDestination()))
                .filter(candidate -> routingKey.equals(candidate.getRoutingKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(RabbitNames.EVENTS_EXCHANGE, binding.getExchange());
        assertEquals(queueName, binding.getDestination());
        assertEquals(routingKey, binding.getRoutingKey());
    }
}
