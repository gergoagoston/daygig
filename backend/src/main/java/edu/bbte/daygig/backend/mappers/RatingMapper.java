package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.rating.RatingOutDto;
import edu.bbte.daygig.backend.model.Rating;

@Component
public class RatingMapper {

    public RatingOutDto toDto(Rating rating) {
        return new RatingOutDto(
                rating.getId(),
                rating.getJob().getId(),
                rating.getReviewer().getId(),
                rating.getReviewee().getId(),
                rating.getContext(),
                rating.getStars(),
                rating.getComment(),
                rating.getCreatedAt());
    }
}
