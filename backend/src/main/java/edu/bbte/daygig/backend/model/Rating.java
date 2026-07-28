package edu.bbte.daygig.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import edu.bbte.daygig.backend.model.enums.RatingContext;

@Entity
@Table(name = "ratings",
        indexes = {
            @Index(name = "idx_ratings_reviewee_context_created_at",
                    columnList = "reviewee_id, context, created_at")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_ratings_job_reviewer",
                    columnNames = {"job_id", "reviewer_id"})
        })
@Getter
@Setter
public class Rating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, updatable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false, updatable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewee_id", nullable = false, updatable = false)
    private User reviewee;

    @Enumerated(EnumType.STRING)
    @Column(name = "context", nullable = false, updatable = false)
    private RatingContext context;

    @Column(name = "stars", nullable = false, updatable = false)
    private int stars;

    @Column(name = "comment", length = 500, updatable = false)
    private String comment;
}
