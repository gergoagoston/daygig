package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.enums.RatingContext;
import edu.bbte.daygig.backend.model.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    boolean existsByJobIdAndReviewerId(UUID jobId,
                                       UUID reviewerId);

    @EntityGraph(attributePaths = {"reviewer", "job"})
    List<Rating> findAllByRevieweeIdAndContextOrderByCreatedAtDesc(UUID revieweeId,
                                                                   RatingContext context);

    @Query("""
            select avg(rating.stars) as average, count(rating.id) as ratingCount
            from Rating rating
            where rating.reviewee.id = :revieweeId
              and rating.context = :context
            """)
    RatingSummaryProjection summarizeByRevieweeIdAndContext(@Param("revieweeId") UUID revieweeId,
                                                             @Param("context") RatingContext context);
}
