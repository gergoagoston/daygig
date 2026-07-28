package edu.bbte.daygig.backend.dto.rating;

import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.model.enums.RatingContext;

public record RatingOutDto(UUID id,
                           UUID jobId,
                           UUID reviewerId,
                           UUID revieweeId,
                           RatingContext context,
                           int stars,
                           String comment,
                           LocalDateTime createdAt) {
}
