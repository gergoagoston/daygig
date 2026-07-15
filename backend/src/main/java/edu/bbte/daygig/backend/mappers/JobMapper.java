package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.job.JobDetailedOutDto;
import edu.bbte.daygig.backend.dto.job.JobOutDto;
import edu.bbte.daygig.backend.model.Job;

@Component
public class JobMapper {

    public JobOutDto toListDto(Job job) {
        return new JobOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(), job.getHourlyRate(),
                job.getCurrency(), job.getLocationName(), job.getCreatedAt());
    }

    public JobDetailedOutDto toDetailsDto(Job job) {
        return new JobDetailedOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(),
                job.getStatus(), job.getHourlyRate(), job.getCurrency(), job.getLocationName(), job.getLatitude(),
                job.getLongitude(), job.getCreatedAt(), job.getUpdatedAt());
    }
}
