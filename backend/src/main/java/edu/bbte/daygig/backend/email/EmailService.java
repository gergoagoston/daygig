package edu.bbte.daygig.backend.email;

public interface EmailService {

    void send(String recipient,
              String subject,
              String body);
}
