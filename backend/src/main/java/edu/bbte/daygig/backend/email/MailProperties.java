package edu.bbte.daygig.backend.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        boolean enabled,
        String from,
        String frontendUrl
) {
}
