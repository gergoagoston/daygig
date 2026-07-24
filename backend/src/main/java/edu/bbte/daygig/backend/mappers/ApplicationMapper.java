package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.application.ApplicationJobOutDto;
import edu.bbte.daygig.backend.dto.application.ApplicationOutDto;
import edu.bbte.daygig.backend.dto.application.OwnerApplicationOutDto;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;

@Component
public class ApplicationMapper {

    public ApplicationOutDto toDto(Application application) {
        return new ApplicationOutDto(application.getId(), application.getStatus(), application.getCreatedAt(),
                toJobDto(application.getJob()));
    }

    public OwnerApplicationOutDto toOwnerDto(Application application) {
        return new OwnerApplicationOutDto(application.getId(), application.getStatus(), application.getCreatedAt(),
                toApplicantDto(application.getApplicant()));
    }

    private ApplicationJobOutDto toJobDto(Job job) {
        return new ApplicationJobOutDto(job.getId(), job.getTitle(), job.getCategory(), job.getLocationName(),
                job.getHourlyRate(), job.getCurrency(), job.getStatus());
    }

    private UserSummaryOutDto toApplicantDto(User applicant) {
        return new UserSummaryOutDto(applicant.getId(), applicant.getFirstName(), applicant.getLastName());
    }
}
