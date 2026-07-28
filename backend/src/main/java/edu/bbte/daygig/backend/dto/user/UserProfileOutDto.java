package edu.bbte.daygig.backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.rating.RatingSummaryOutDto;

public record UserProfileOutDto(UUID id, String firstName, String lastName, String bio, String avatarUrl,
                                LocalDateTime createdAt, RatingSummaryOutDto workerRating,
                                RatingSummaryOutDto jobOwnerRating, long completedJobsAsWorker,
                                long completedJobsAsOwner) {
}
