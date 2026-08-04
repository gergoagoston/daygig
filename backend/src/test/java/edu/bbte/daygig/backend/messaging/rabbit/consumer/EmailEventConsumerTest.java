package edu.bbte.daygig.backend.messaging.rabbit.consumer;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import edu.bbte.daygig.backend.email.EmailService;
import edu.bbte.daygig.backend.email.MailProperties;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailEventConsumerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private EmailService emailService;

    @Test
    void shouldSendApplicationRejectionEmail() {
        UUID recipientId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        User recipient = new User();
        recipient.setId(recipientId);
        recipient.setFirstName("Gergo");
        recipient.setEmail("gergo@example.com");
        Job job = new Job();
        job.setId(jobId);
        job.setTitle("Garden cleaning");
        DomainEventEnvelope<JobActivityEventPayload> event = DomainEventEnvelope.create(
                RabbitNames.APPLICATION_REJECTED,
                recipientId,
                new JobActivityEventPayload(jobId, applicationId)
        );
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        EmailEventConsumer consumer = new EmailEventConsumer(
                userRepository,
                jobRepository,
                emailService,
                new MailProperties(true, "daygig@example.com", "https://daygig.example.com/")
        );

        consumer.consume(event);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(
                eq("gergo@example.com"),
                eq("Update on your DayGig application"),
                bodyCaptor.capture()
        );
        assertTrue(bodyCaptor.getValue().contains("Garden cleaning"));
        assertTrue(bodyCaptor.getValue().contains("https://daygig.example.com/jobs/" + jobId));
    }
}
