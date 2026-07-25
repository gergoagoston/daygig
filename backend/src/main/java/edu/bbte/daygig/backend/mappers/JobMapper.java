package edu.bbte.daygig.backend.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.job.JobCreateInDto;
import edu.bbte.daygig.backend.dto.job.JobDetailedOutDto;
import edu.bbte.daygig.backend.dto.job.JobOutDto;
import edu.bbte.daygig.backend.model.Job;

@Component
@RequiredArgsConstructor
public class JobMapper {

    private static final int PUBLIC_LOCATION_SCALE = 2;
    private static final int PUBLIC_LOCATION_RADIUS_METERS = 1000;

    private final UserMapper userMapper;

    public Job fromCreateDto(JobCreateInDto jobCreateInDto) {
        Job job = new Job();
        job.setTitle(jobCreateInDto.title());
        job.setDescription(jobCreateInDto.description());
        job.setCategory(jobCreateInDto.category());
        job.setHourlyRate(jobCreateInDto.hourlyRate());
        job.setCurrency(jobCreateInDto.currency());
        job.setLocationName(jobCreateInDto.locationName());
        job.setLatitude(jobCreateInDto.latitude());
        job.setLongitude(jobCreateInDto.longitude());
        return job;
    }

    public JobOutDto toListDto(Job job) {
        return new JobOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(), job.getStatus(),
                job.getHourlyRate(), job.getCurrency(), job.getLocationName(), job.getCreatedAt(),
                completedApplicationId(job), job.getCompletedAt());
    }

    public JobDetailedOutDto toDetailsDto(Job job) {
        return new JobDetailedOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(),
                job.getStatus(), job.getHourlyRate(), job.getCurrency(), job.getLocationName(),
                approximateCoordinate(job.getLatitude()), approximateCoordinate(job.getLongitude()),
                PUBLIC_LOCATION_RADIUS_METERS, userMapper.toSummaryDto(job.getOwner()), job.getCreatedAt(),
                completedApplicationId(job), job.getCompletedAt());
    }

    private UUID completedApplicationId(Job job) {
        return job.getCompletedApplication() == null ? null : job
                .getCompletedApplication()
                .getId();
    }

    private BigDecimal approximateCoordinate(BigDecimal coordinate) {
        if (coordinate == null) {
            return null;
        }

        return coordinate.setScale(PUBLIC_LOCATION_SCALE, RoundingMode.HALF_UP);
    }
}
