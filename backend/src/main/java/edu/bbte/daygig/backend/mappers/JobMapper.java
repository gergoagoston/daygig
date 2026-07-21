package edu.bbte.daygig.backend.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.job.JobDetailedOutDto;
import edu.bbte.daygig.backend.dto.job.JobOutDto;
import edu.bbte.daygig.backend.model.Job;

@Component
@RequiredArgsConstructor
public class JobMapper {

    private static final int PUBLIC_LOCATION_SCALE = 2;
    private static final int PUBLIC_LOCATION_RADIUS_METERS = 1000;

    private final UserMapper userMapper;

    public JobOutDto toListDto(Job job) {
        return new JobOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(), job.getStatus(),
                job.getHourlyRate(), job.getCurrency(), job.getLocationName(), job.getCreatedAt());
    }

    public JobDetailedOutDto toDetailsDto(Job job) {
        return new JobDetailedOutDto(job.getId(), job.getTitle(), job.getDescription(), job.getCategory(),
                job.getStatus(), job.getHourlyRate(), job.getCurrency(), job.getLocationName(),
                approximateCoordinate(job.getLatitude()), approximateCoordinate(job.getLongitude()),
                PUBLIC_LOCATION_RADIUS_METERS, userMapper.toSummaryDto(job.getOwner()), job.getCreatedAt());
    }

    private BigDecimal approximateCoordinate(BigDecimal coordinate) {
        if (coordinate == null) {
            return null;
        }

        return coordinate.setScale(PUBLIC_LOCATION_SCALE, RoundingMode.HALF_UP);
    }
}
