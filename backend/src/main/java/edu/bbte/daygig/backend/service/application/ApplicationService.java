package edu.bbte.daygig.backend.service.application;

import java.util.List;
import java.util.UUID;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;

public interface ApplicationService {

    Application apply(UUID jobId,
                      UUID applicantId);

    List<Application> getMine(UUID applicantId);

    List<Application> getByJobForOwner(UUID jobId,
                                       UUID ownerId);

    Application updateStatus(UUID applicationId,
                             ApplicationStatus status,
                             UUID actorId);
}
