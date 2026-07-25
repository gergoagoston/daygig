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
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;

@Entity
@Table(name = "applications",
        indexes = {
            @Index(name = "idx_applications_applicant_id", columnList = "applicant_id"),
            @Index(name = "idx_applications_job_id", columnList = "job_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_applications_applicant_job", columnNames = {"applicant_id", "job_id"})
        })
@Getter
@Setter
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.ACTIVE;
}
