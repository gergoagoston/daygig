package edu.bbte.daygig.backend.repository;

import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findAllByStatus(JobStatus status, Pageable pageable);

    boolean existsByTitle(String title);
}
