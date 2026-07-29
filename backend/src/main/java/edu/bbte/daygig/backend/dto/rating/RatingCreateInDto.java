package edu.bbte.daygig.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RatingCreateInDto(
        @NotNull(message = "Star rating is required")
        @Min(value = 1, message = "Star rating must be at least 1")
        @Max(value = 5, message = "Star rating must be at most 5")
        Integer stars,

        @Size(max = 500, message = "Review cannot exceed 500 characters")
        String comment) {
}
