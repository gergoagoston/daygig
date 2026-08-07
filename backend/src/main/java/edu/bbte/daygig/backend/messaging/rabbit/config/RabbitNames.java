package edu.bbte.daygig.backend.messaging.rabbit.config;

public final class RabbitNames {

    public static final String EVENTS_EXCHANGE = "daygig.events";
    public static final String NOTIFICATION_QUEUE = "daygig.notification";
    public static final String EMAIL_QUEUE = "daygig.email";

    public static final String APPLICATION_CREATED = "application.created";
    public static final String APPLICATION_REJECTED = "application.rejected";
    public static final String APPLICATION_WITHDRAWN = "application.withdrawn";
    public static final String JOB_COMPLETED = "job.completed";
    public static final String CONVERSATION_STARTED = "conversation.started";
    public static final String MESSAGE_CREATED = "message.created";

    private RabbitNames() {
    }
}
