package edu.bbte.daygig.backend.dto.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;

public record JobOutDto(UUID id, String title, String description, JobCategory category, JobStatus status,
                        BigDecimal hourlyRate, Currency currency, String locationName, LocalDateTime createdAt) {
}
