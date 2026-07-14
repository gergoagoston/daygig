package edu.bbte.daygig.backend.service.job;

import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Override
    public Page<Job> getOpenJobs(Pageable pageable) {
        return jobRepository.findAllByStatus(JobStatus.OPEN, pageable);
    }

    @Override
    public Job getById(UUID id) {
        return jobRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }
}
