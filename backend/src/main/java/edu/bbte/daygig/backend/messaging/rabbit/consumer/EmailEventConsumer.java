package edu.bbte.daygig.backend.messaging.rabbit.consumer;

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
        if (!RabbitNames.APPLICATION_REJECTED.equals(event.eventType())) {
            LOG.warn(
                    "Ignoring unsupported email event. eventId={}, eventType={}",
                    event.eventId(),
                    event.eventType()
            );
            return;
        }

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

        emailService.send(
                recipient.getEmail(),
                REJECTION_SUBJECT,
                rejectionBody(recipient, job)
        );

        LOG.info(
                "Application rejection email sent. eventId={}, recipientUserId={}",
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

    private String frontendUrl() {
        String configuredUrl = mailProperties.frontendUrl();
        if (configuredUrl.endsWith("/")) {
            return configuredUrl.substring(0, configuredUrl.length() - 1);
        }
        return configuredUrl;
    }
}
