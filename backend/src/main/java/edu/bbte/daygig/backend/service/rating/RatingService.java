package edu.bbte.daygig.backend.service.rating;

import java.util.UUID;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
import edu.bbte.daygig.backend.model.Rating;

public interface RatingService {

    Rating create(UUID jobId,
                  RatingCreateInDto ratingCreateInDto,
                  UUID reviewerId);
}
