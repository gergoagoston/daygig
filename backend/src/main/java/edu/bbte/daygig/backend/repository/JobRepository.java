package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    @EntityGraph(attributePaths = "completedApplication")
    List<Job> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    @Override
    @EntityGraph(attributePaths = {"owner", "completedApplication"})
    Optional<Job> findById(UUID id);

    boolean existsByTitle(String title);
}
