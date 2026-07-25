package edu.bbte.daygig.backend.service.job;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import edu.bbte.daygig.backend.dto.job.JobCreateInDto;
import edu.bbte.daygig.backend.dto.job.JobSearchInDto;
import edu.bbte.daygig.backend.model.Job;

public interface JobService {

    Page<Job> getOpenJobs(JobSearchInDto search,
                          Pageable pageable);

    List<Job> getMine(UUID ownerId);

    Job create(JobCreateInDto jobCreateInDto,
               UUID ownerId);

    Job complete(UUID jobId,
                 UUID applicationId,
                 UUID ownerId);

    Job getById(UUID id);
}
