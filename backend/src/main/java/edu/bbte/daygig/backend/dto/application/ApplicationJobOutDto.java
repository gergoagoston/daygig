package edu.bbte.daygig.backend.dto.application;

import java.math.BigDecimal;
import java.util.UUID;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;

public record ApplicationJobOutDto(UUID id, String title, JobCategory category, String locationName,
                                   BigDecimal hourlyRate, Currency currency, JobStatus status) {
}
