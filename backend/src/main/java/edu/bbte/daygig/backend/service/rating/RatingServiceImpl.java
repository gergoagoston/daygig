package edu.bbte.daygig.backend.service.rating;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
import edu.bbte.daygig.backend.dto.rating.RatingEligibilityOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingParticipantOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingSummaryOutDto;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
import edu.bbte.daygig.backend.exception.ForbiddenException;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.exception.ServiceException;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.RatingContext;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.Rating;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.RatingRepository;
import edu.bbte.daygig.backend.repository.RatingSummaryProjection;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.service.avatar.UserAvatarService;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final UserAvatarService userAvatarService;

    @Override
    @Transactional
    public RatingOutDto create(UUID jobId,
                               RatingCreateInDto ratingCreateInDto,
                               UUID reviewerId) {
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        validateCompletedJob(job);

        RatingParticipants participants = resolveParticipants(job, reviewerId);
        if (participants == null) {
            throw new ForbiddenException("Only participants of the completed job can leave a rating");
        }

        if (participants.reviewer().getId().equals(participants.reviewee().getId())) {
            throw new ForbiddenException("Users cannot rate themselves");
        }

        if (ratingRepository.existsByJobIdAndReviewerId(jobId, reviewerId)) {
            throw new DuplicateResourceException("You have already rated this job");
        }

        Rating rating = new Rating();
        rating.setJob(job);
        rating.setReviewer(participants.reviewer());
        rating.setReviewee(participants.reviewee());
        rating.setContext(participants.context());
        rating.setStars(ratingCreateInDto.stars());
        rating.setComment(normalizeComment(ratingCreateInDto.comment()));

        return toDto(ratingRepository.saveAndFlush(rating));
    }

    @Override
    @Transactional(readOnly = true)
    public RatingEligibilityOutDto getEligibility(UUID jobId,
                                                  UUID reviewerId) {
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (job.getStatus() != JobStatus.COMPLETED || job.getCompletedApplication() == null) {
            return ineligible();
        }

        RatingParticipants participants = resolveParticipants(job, reviewerId);
        if (participants == null
                || participants.reviewer().getId().equals(participants.reviewee().getId())) {
            return ineligible();
        }

        boolean alreadyRated = ratingRepository.existsByJobIdAndReviewerId(jobId, reviewerId);
        return new RatingEligibilityOutDto(
                true,
                alreadyRated,
                toParticipantDto(participants.reviewee()),
                participants.context());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingOutDto> getReceivedRatings(UUID revieweeId,
                                                 RatingContext context) {
        ensureUserExists(revieweeId);
        return ratingRepository
                .findAllByRevieweeIdAndContextOrderByCreatedAtDesc(revieweeId, context)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryOutDto getSummary(UUID revieweeId,
                                          RatingContext context) {
        RatingSummaryProjection projection = ratingRepository
                .summarizeByRevieweeIdAndContext(revieweeId, context);
        BigDecimal average = projection.getAverage() == null
                ? null
                : BigDecimal.valueOf(projection.getAverage()).setScale(1, RoundingMode.HALF_UP);
        return new RatingSummaryOutDto(average, projection.getRatingCount());
    }

    private void validateCompletedJob(Job job) {
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new ServiceException("Ratings can only be created for completed jobs");
        }

        if (job.getCompletedApplication() == null) {
            throw new ServiceException("Completed job does not have a completed application");
        }
    }

    private RatingParticipants resolveParticipants(Job job,
                                                   UUID reviewerId) {
        Application completedApplication = job.getCompletedApplication();
        User owner = job.getOwner();
        User worker = completedApplication.getApplicant();

        if (owner.getId().equals(reviewerId)) {
            return new RatingParticipants(owner, worker, RatingContext.WORKER);
        }

        if (worker.getId().equals(reviewerId)) {
            return new RatingParticipants(worker, owner, RatingContext.JOB_OWNER);
        }

        return null;
    }

    private void ensureUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        return normalizedComment.isEmpty() ? null : normalizedComment;
    }

    private RatingEligibilityOutDto ineligible() {
        return new RatingEligibilityOutDto(false, false, null, null);
    }

    private RatingOutDto toDto(Rating rating) {
        return new RatingOutDto(
                rating.getId(),
                rating.getStars(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getContext(),
                toParticipantDto(rating.getReviewer()),
                rating.getJob().getId(),
                rating.getJob().getTitle());
    }

    private RatingParticipantOutDto toParticipantDto(User user) {
        return new RatingParticipantOutDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                userAvatarService.getAvatarUrl(user.getId()));
    }

    private record RatingParticipants(User reviewer,
                                      User reviewee,
                                      RatingContext context) {
    }
}
