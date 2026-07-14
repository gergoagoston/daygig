package edu.bbte.daygig.backend.dto.job;

import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobOutDto(UUID id,
                        String title,
                        String description,
                        JobCategory category,
                        BigDecimal hourlyRate,
                        Currency currency,
                        String locationName,
                        LocalDateTime createdAt) {
}
