package edu.bbte.daygig.backend.dto.job;

import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobDetailedOutDto(UUID id,
                                String title,
                                String description,
                                JobCategory category,
                                JobStatus status,
                                BigDecimal hourlyRate,
                                Currency currency,
                                String locationName,
                                BigDecimal latitude,
                                BigDecimal longitude,
                                LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
}
