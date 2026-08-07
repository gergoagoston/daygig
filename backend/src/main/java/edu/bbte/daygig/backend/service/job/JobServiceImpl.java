package edu.bbte.daygig.backend.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.job.JobCreateInDto;
import edu.bbte.daygig.backend.dto.job.JobSearchInDto;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
import edu.bbte.daygig.backend.exception.ForbiddenException;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.exception.ServiceException;
import edu.bbte.daygig.backend.mappers.JobMapper;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.repository.JobApplicationCountProjection;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.JobSpecifications;
import edu.bbte.daygig.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private static final Set<String> ALLOWED_PUBLIC_SORT_PROPERTIES = Set.of(
            "createdAt", "hourlyRate");

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Page<Job> getOpenJobs(JobSearchInDto search,
                                 Pageable pageable) {
        validatePublicSort(pageable.getSort());
        return jobRepository.findAll(JobSpecifications.openJobsMatching(search), pageable);
    }

    @Override
    public List<OwnerJobSummary> getMine(UUID ownerId) {
        List<Job> jobs = jobRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        Map<UUID, Long> applicationCounts = applicationRepository
                .countByJobOwnerId(ownerId)
                .stream()
                .collect(Collectors.toMap(
                        JobApplicationCountProjection::getJobId,
                        JobApplicationCountProjection::getApplicationCount
                ));

        return jobs
                .stream()
                .map(job -> new OwnerJobSummary(
                        job,
                        applicationCounts.getOrDefault(job.getId(), 0L)
                ))
                .toList();
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
    @Transactional
    public Job complete(UUID jobId,
                        UUID applicationId,
                        UUID ownerId) {
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        ensureOwner(job, ownerId);

        if (job.getCompletedApplication() != null) {
            if (job
                    .getCompletedApplication()
                    .getId()
                    .equals(applicationId)) {
                return job;
            }

            throw new DuplicateResourceException("Job has already been completed by another applicant");
        }

        if (job.getStatus() != JobStatus.OPEN) {
            throw new ServiceException("Only open jobs can be completed");
        }

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + applicationId));

        if (!application
                .getJob()
                .getId()
                .equals(jobId)) {
            throw new ServiceException("Application does not belong to this job");
        }

        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ServiceException("Only active applications can complete a job");
        }

        job.setCompletedApplication(application);
        job.setCompletedAt(LocalDateTime.now());
        job.setStatus(JobStatus.COMPLETED);

        Job savedJob = jobRepository.saveAndFlush(job);
        eventPublisher.publishEvent(DomainEventEnvelope.create(
                RabbitNames.JOB_COMPLETED,
                application.getApplicant().getId(),
                new JobActivityEventPayload(
                        savedJob.getId(),
                        application.getId()
                )
        ));
        return savedJob;
    }

    @Override
    public Job getById(UUID id) {
        return jobRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    private void ensureOwner(Job job,
                             UUID ownerId) {
        if (!job
                .getOwner()
                .getId()
                .equals(ownerId)) {
            throw new ForbiddenException("Only the job owner can complete this job");
        }
    }

    private void validatePublicSort(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED_PUBLIC_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new ServiceException("Unsupported job sort property: " + order.getProperty());
            }
        });
    }

}
