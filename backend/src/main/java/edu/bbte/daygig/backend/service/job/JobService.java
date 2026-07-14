package edu.bbte.daygig.backend.service.job;

import edu.bbte.daygig.backend.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JobService {

    Page<Job> getOpenJobs(Pageable pageable);

    Job getById(UUID id);
}
