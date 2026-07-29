package edu.bbte.daygig.backend.dto.rating;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.bbte.daygig.backend.model.enums.RatingContext;

public record RatingEligibilityOutDto(boolean eligible,
                                      boolean alreadyRated,
                                      @JsonInclude(JsonInclude.Include.ALWAYS)
                                      RatingParticipantOutDto reviewee,
                                      @JsonInclude(JsonInclude.Include.ALWAYS)
                                      RatingContext context) {
}
