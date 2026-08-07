package edu.bbte.daygig.backend.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conversations",
        indexes = @Index(name = "idx_conversations_last_message_at", columnList = "last_message_at"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversations_application",
                columnNames = "application_id"
        ))
@Getter
@Setter
public class Conversation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "last_message", nullable = false, length = 200)
    private String lastMessage;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;
}
