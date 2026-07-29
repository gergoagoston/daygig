package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    @Override
    @EntityGraph(attributePaths = {"applicant", "job.owner"})
    Optional<Application> findById(UUID id);

    boolean existsByApplicantIdAndJobId(UUID applicantId,
                                        UUID jobId);

    @EntityGraph(attributePaths = {"job", "job.completedApplication"})
    List<Application> findAllByApplicantId(UUID applicantId);

    @EntityGraph(attributePaths = {"applicant", "job.owner"})
    List<Application> findAllByJobIdOrderByCreatedAtDesc(UUID jobId);
}
