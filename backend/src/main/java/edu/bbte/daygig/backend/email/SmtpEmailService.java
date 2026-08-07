package edu.bbte.daygig.backend.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "app.mail",
        name = "enabled",
        havingValue = "true"
)
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public SmtpEmailService(JavaMailSender mailSender,
                            MailProperties mailProperties) {
        if (mailProperties.from() == null || mailProperties.from().isBlank()) {
            throw new IllegalStateException("MAIL_FROM or MAIL_USERNAME must be configured when email is enabled");
        }
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(String recipient,
                     String subject,
                     String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.from());
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
