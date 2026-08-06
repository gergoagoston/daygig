package edu.bbte.daygig.backend.messaging.rabbit.consumer;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.email.EmailService;
import edu.bbte.daygig.backend.email.MailProperties;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;

@Component
@ConditionalOnProperty(
        prefix = "app.mail",
        name = "enabled",
        havingValue = "true"
)
public class EmailEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(EmailEventConsumer.class);
    private static final String REJECTION_SUBJECT = "Update on your DayGig application";
    private static final String CONVERSATION_SUBJECT = "A conversation was started on DayGig";

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmailService emailService;
    private final MailProperties mailProperties;

    public EmailEventConsumer(UserRepository userRepository,
                              JobRepository jobRepository,
                              EmailService emailService,
                              MailProperties mailProperties) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.emailService = emailService;
        this.mailProperties = mailProperties;
    }

    @RabbitListener(queues = RabbitNames.EMAIL_QUEUE)
    public void consume(DomainEventEnvelope<JobActivityEventPayload> event) {
        User recipient = userRepository
                .findById(event.recipientUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Email recipient not found with id: " + event.recipientUserId()
                ));
        Job job = jobRepository
                .findById(event.payload().jobId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + event.payload().jobId()
                ));

        switch (event.eventType()) {
            case RabbitNames.APPLICATION_REJECTED -> sendRejectionEmail(event, recipient, job);
            case RabbitNames.CONVERSATION_STARTED -> sendConversationStartedEmail(
                    event,
                    recipient,
                    job
            );
            default -> LOG.warn(
                    "Ignoring unsupported email event. eventId={}, eventType={}",
                    event.eventId(),
                    event.eventType()
            );
        }
    }

    private void sendRejectionEmail(DomainEventEnvelope<JobActivityEventPayload> event,
                                    User recipient,
                                    Job job) {
        emailService.send(recipient.getEmail(), REJECTION_SUBJECT, rejectionBody(recipient, job));
        logSent(event, "Application rejection");
    }

    private void sendConversationStartedEmail(
            DomainEventEnvelope<JobActivityEventPayload> event,
            User recipient,
            Job job
    ) {
        User sender = userRepository
                .findById(event.payload().senderUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation sender not found with id: " + event.payload().senderUserId()
                ));

        emailService.send(
                recipient.getEmail(),
                CONVERSATION_SUBJECT,
                conversationStartedBody(recipient, sender, job, event.payload().conversationId())
        );
        logSent(event, "Conversation started");
    }

    private void logSent(DomainEventEnvelope<JobActivityEventPayload> event,
                         String emailType) {
        LOG.info(
                "{} email sent. eventId={}, recipientUserId={}",
                emailType,
                event.eventId(),
                event.recipientUserId()
        );
    }

    private String rejectionBody(User recipient,
                                 Job job) {
        return """
                Hello %s,

                You were not selected for the job \"%s\".

                You can view the job here:
                %s/jobs/%s

                DayGig
                """.formatted(
                recipient.getFirstName(),
                job.getTitle(),
                frontendUrl(),
                job.getId()
        );
    }

    private String conversationStartedBody(User recipient,
                                           User sender,
                                           Job job,
                                           UUID conversationId) {
        return """
                Hello %s,

                %s %s started a conversation with you about "%s".

                You can open the conversation here:
                %s/conversations/%s

                DayGig
                """.formatted(
                recipient.getFirstName(),
                sender.getFirstName(),
                sender.getLastName(),
                job.getTitle(),
                frontendUrl(),
                conversationId
        );
    }

    private String frontendUrl() {
        String configuredUrl = mailProperties.frontendUrl();
        if (configuredUrl.endsWith("/")) {
            return configuredUrl.substring(0, configuredUrl.length() - 1);
        }
        return configuredUrl;
    }
}
