package edu.bbte.daygig.backend.service.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
import edu.bbte.daygig.backend.exception.ForbiddenException;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.exception.ServiceException;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.service.job.JobService;
import edu.bbte.daygig.backend.service.user.UserService;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final UserService userService;

    @Override
    @Transactional
    public Application apply(UUID jobId,
                             UUID applicantId) {
        User applicant = userService.getById(applicantId);
        Job job = jobService.getById(jobId);

        validateNewApplication(job, applicantId, jobId);

        Application application = new Application();
        application.setApplicant(applicant);
        application.setJob(job);
        application.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.saveAndFlush(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getMine(UUID applicantId) {
        userService.getById(applicantId);
        return applicationRepository.findAllByApplicantId(applicantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> getByJobForOwner(UUID jobId,
                                              UUID ownerId) {
        Job job = jobService.getById(jobId);
        ensureOwner(job, ownerId);

        return applicationRepository.findAllByJobIdOrderByCreatedAtDesc(jobId);
    }

    @Override
    @Transactional
    public Application updateStatus(UUID applicationId,
                                    ApplicationStatus status,
                                    UUID ownerId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        Job job = application.getJob();

        ensureOwner(job, ownerId);
        ensureJobIsNotCompleted(job);
        applyStatusTransition(application, status);

        return applicationRepository.saveAndFlush(application);
    }

    private void validateNewApplication(Job job,
                                        UUID applicantId,
                                        UUID jobId) {
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ServiceException("Cannot apply to a job that is not open");
        }

        if (job
                .getOwner()
                .getId()
                .equals(applicantId)) {
            throw new ServiceException("Cannot apply to your own job");
        }

        if (applicationRepository.existsByApplicantIdAndJobId(applicantId, jobId)) {
            throw new DuplicateResourceException("You have already applied to this job");
        }
    }

    private void applyStatusTransition(Application application,
                                       ApplicationStatus status) {
        if (status == null) {
            throw unsupportedStatus();
        }

        switch (status) {
            case ACCEPTED -> accept(application);
            case REJECTED -> reject(application);
            case COMPLETED -> complete(application);
            default -> throw unsupportedStatus();
        }
    }

    private void accept(Application application) {
        Job job = application.getJob();

        ensureApplicationStatusIsAnyOf(application, "Only pending applications can be accepted",
                ApplicationStatus.PENDING);

        if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.ASSIGNED) {
            throw new ServiceException("Only open or assigned jobs can accept applications");
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        job.setStatus(JobStatus.ASSIGNED);
    }

    private void reject(Application application) {
        ensureApplicationStatusIsAnyOf(application, "Only pending or accepted applications can be rejected",
                ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED);

        application.setStatus(ApplicationStatus.REJECTED);
    }

    private void complete(Application application) {
        Job job = application.getJob();

        ensureApplicationStatusIsAnyOf(application, "Only accepted applications can be completed",
                ApplicationStatus.ACCEPTED);

        if (job.getStatus() != JobStatus.ASSIGNED) {
            throw new ServiceException("Only assigned jobs can be completed");
        }

        application.setStatus(ApplicationStatus.COMPLETED);
        job.setStatus(JobStatus.COMPLETED);
        rejectOtherActiveApplications(application);
    }

    private void ensureJobIsNotCompleted(Job job) {
        if (job.getStatus() == JobStatus.COMPLETED) {
            throw new ServiceException("Cannot update applications for a completed job");
        }
    }

    private void ensureOwner(Job job,
                             UUID ownerId) {
        if (!job
                .getOwner()
                .getId()
                .equals(ownerId)) {
            throw new ForbiddenException("Only the job owner can manage applications for this job");
        }
    }

    private void ensureApplicationStatusIsAnyOf(Application application,
                                                String errorMessage,
                                                ApplicationStatus... allowedStatuses) {
        for (ApplicationStatus allowedStatus : allowedStatuses) {
            if (application.getStatus() == allowedStatus) {
                return;
            }
        }

        throw new ServiceException(errorMessage);
    }

    private void rejectOtherActiveApplications(Application completedApplication) {
        applicationRepository
                .findAllByJobIdOrderByCreatedAtDesc(completedApplication
                        .getJob()
                        .getId())
                .stream()
                .filter(application -> !isSameApplication(application, completedApplication))
                .filter(this::isPendingOrAccepted)
                .forEach(application -> application.setStatus(ApplicationStatus.REJECTED));
    }

    private boolean isSameApplication(Application firstApplication,
                                      Application secondApplication) {
        return firstApplication
                .getId()
                .equals(secondApplication.getId());
    }

    private boolean isPendingOrAccepted(Application application) {
        return application.getStatus() == ApplicationStatus.PENDING
                || application.getStatus() == ApplicationStatus.ACCEPTED;
    }

    private ServiceException unsupportedStatus() {
        return new ServiceException("Application status can only be changed to ACCEPTED, REJECTED or COMPLETED");
    }
}
