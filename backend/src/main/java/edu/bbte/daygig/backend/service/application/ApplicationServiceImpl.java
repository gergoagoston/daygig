package edu.bbte.daygig.backend.service.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
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
}
