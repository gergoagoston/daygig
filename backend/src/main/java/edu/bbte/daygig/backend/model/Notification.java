package edu.bbte.daygig.backend.model;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications", indexes = {
        @Index(
                name = "idx_notifications_recipient_read_created",
                columnList = "recipient_id, is_read, created_at"
        )
})
@Getter
@Setter
public class Notification extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "is_read", nullable = false)
    private boolean read;
}
