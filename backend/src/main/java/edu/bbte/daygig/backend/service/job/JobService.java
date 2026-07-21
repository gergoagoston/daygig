package edu.bbte.daygig.backend.service.job;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import edu.bbte.daygig.backend.model.Job;

public interface JobService {

    Page<Job> getOpenJobs(Pageable pageable);

    List<Job> getMine(UUID ownerId);

    Job getById(UUID id);
}
