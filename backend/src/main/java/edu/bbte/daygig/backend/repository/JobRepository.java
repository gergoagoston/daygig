package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findAllByStatus(JobStatus status,
                              Pageable pageable);

    @EntityGraph(attributePaths = "completedApplication")
    List<Job> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    @Override
    @EntityGraph(attributePaths = {"owner", "completedApplication"})
    Optional<Job> findById(UUID id);

    boolean existsByTitle(String title);
}
