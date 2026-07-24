package edu.bbte.daygig.backend.service.application;

import java.util.List;
import java.util.UUID;
import edu.bbte.daygig.backend.model.Application;

public interface ApplicationService {

    Application apply(UUID jobId,
                      UUID applicantId);

    List<Application> getMine(UUID applicantId);
}
