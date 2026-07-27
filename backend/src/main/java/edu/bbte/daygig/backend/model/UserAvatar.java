package edu.bbte.daygig.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_avatars")
@Getter
@Setter
public class UserAvatar {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_data", nullable = false, length = 2 * 1024 * 1024)
    private byte[] imageData;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
