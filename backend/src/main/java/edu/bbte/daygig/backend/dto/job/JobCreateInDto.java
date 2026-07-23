package edu.bbte.daygig.backend.dto.job;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;

public record JobCreateInDto(@NotBlank @Size(max = 50) String title,
                             @NotBlank @Size(max = 1000) String description,
                             @NotNull JobCategory category,
                             @NotNull @Positive BigDecimal hourlyRate,
                             @NotNull Currency currency,
                             @NotBlank String locationName,
                             @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
                             @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {
}
