package edu.bbte.daygig.backend.service.rating;

import java.util.List;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
import edu.bbte.daygig.backend.dto.rating.RatingEligibilityOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingSummaryOutDto;
import edu.bbte.daygig.backend.model.enums.RatingContext;

public interface RatingService {

    RatingOutDto create(UUID jobId,
                        RatingCreateInDto ratingCreateInDto,
                        UUID reviewerId);

    RatingEligibilityOutDto getEligibility(UUID jobId,
                                           UUID reviewerId);

    List<RatingOutDto> getReceivedRatings(UUID revieweeId,
                                          RatingContext context);

    RatingSummaryOutDto getSummary(UUID revieweeId,
                                   RatingContext context);
}
