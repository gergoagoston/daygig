package edu.bbte.daygig.backend.dto.job;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import edu.bbte.daygig.backend.model.enums.JobCategory;

public record JobSearchInDto(
        @Size(max = 100) String q,
        JobCategory category,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal minHourlyRate,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal maxHourlyRate,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @PositiveOrZero @DecimalMax("200.0") BigDecimal maxDistanceKm) {

    @AssertTrue(message = "Minimum hourly rate must not be greater than maximum hourly rate")
    public boolean isHourlyRateRangeValid() {
        return minHourlyRate == null
                || maxHourlyRate == null
                || minHourlyRate.compareTo(maxHourlyRate) <= 0;
    }

    @AssertTrue(message = "Latitude, longitude and maximum distance must be provided together")
    public boolean isDistanceFilterComplete() {
        boolean noneProvided = latitude == null && longitude == null && maxDistanceKm == null;
        return noneProvided || hasDistanceFilter();
    }

    public boolean hasDistanceFilter() {
        return latitude != null && longitude != null && maxDistanceKm != null;
    }
}
