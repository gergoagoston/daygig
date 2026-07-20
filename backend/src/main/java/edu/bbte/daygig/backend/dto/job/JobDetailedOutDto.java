package edu.bbte.daygig.backend.dto.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;

public record JobDetailedOutDto(UUID id, String title, String description, JobCategory category, JobStatus status,
                                BigDecimal hourlyRate, Currency currency, String locationName,
                                BigDecimal approximateLatitude, BigDecimal approximateLongitude,
                                Integer locationRadiusMeters, UserSummaryOutDto owner, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
