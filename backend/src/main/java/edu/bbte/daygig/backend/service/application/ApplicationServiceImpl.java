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
        application.setStatus(ApplicationStatus.ACTIVE);

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
                                    UUID actorId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        ensureJobIsOpen(application.getJob());
        applyStatusTransition(application, status, actorId);

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
                                       ApplicationStatus status,
                                       UUID actorId) {
        if (status == null) {
            throw unsupportedStatus();
        }

        switch (status) {
            case REJECTED -> reject(application, actorId);
            case WITHDRAWN -> withdraw(application, actorId);
            default -> throw unsupportedStatus();
        }
    }

    private void reject(Application application,
                        UUID actorId) {
        ensureOwner(application.getJob(), actorId);
        ensureActive(application);
        application.setStatus(ApplicationStatus.REJECTED);
    }

    private void withdraw(Application application,
                          UUID actorId) {
        ensureApplicant(application, actorId);
        ensureActive(application);
        application.setStatus(ApplicationStatus.WITHDRAWN);
    }

    private void ensureJobIsOpen(Job job) {
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ServiceException("Applications can only be updated for open jobs");
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

    private void ensureApplicant(Application application,
                                 UUID applicantId) {
        if (!application
                .getApplicant()
                .getId()
                .equals(applicantId)) {
            throw new ForbiddenException("Only the applicant can withdraw this application");
        }
    }

    private void ensureActive(Application application) {
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ServiceException("Only active applications can be updated");
        }
    }

    private ServiceException unsupportedStatus() {
        return new ServiceException("Application status can only be changed to REJECTED or WITHDRAWN");
    }
}
