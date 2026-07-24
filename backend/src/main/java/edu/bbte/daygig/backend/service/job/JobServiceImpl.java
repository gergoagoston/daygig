package edu.bbte.daygig.backend.service.job;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.job.JobCreateInDto;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.mappers.JobMapper;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    public Page<Job> getOpenJobs(Pageable pageable) {
        return jobRepository.findAllByStatus(JobStatus.OPEN, pageable);
    }

    @Override
    public List<Job> getMine(UUID ownerId) {
        return jobRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Override
    public Job create(JobCreateInDto jobCreateInDto,
                      UUID ownerId) {
        User owner = userRepository
                .findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));

        Job job = jobMapper.fromCreateDto(jobCreateInDto);
        job.setStatus(JobStatus.OPEN);
        job.setOwner(owner);

        return jobRepository.save(job);
    }

    @Override
    public Job getById(UUID id) {
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }
}
