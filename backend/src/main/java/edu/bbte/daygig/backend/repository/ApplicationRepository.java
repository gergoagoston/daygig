package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.Application;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    boolean existsByApplicantIdAndJobId(UUID applicantId,
                                        UUID jobId);

    @EntityGraph(attributePaths = "job")
    List<Application> findAllByApplicantId(UUID applicantId);
}
