package edu.bbte.daygig.backend.dto.job;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import edu.bbte.daygig.backend.model.enums.JobCategory;

public record JobSearchInDto(
        @Size(max = 100) String q,
        JobCategory category,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal minHourlyRate,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal maxHourlyRate) {

    @AssertTrue(message = "Minimum hourly rate must not be greater than maximum hourly rate")
    public boolean isHourlyRateRangeValid() {
        return minHourlyRate == null
                || maxHourlyRate == null
                || minHourlyRate.compareTo(maxHourlyRate) <= 0;
    }
}
