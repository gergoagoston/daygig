package edu.bbte.daygig.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    boolean existsByJobIdAndReviewerId(UUID jobId,
                                       UUID reviewerId);
}
