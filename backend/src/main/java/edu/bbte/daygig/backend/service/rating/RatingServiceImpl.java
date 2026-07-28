package edu.bbte.daygig.backend.service.rating;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
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

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public Rating create(UUID jobId,
                         RatingCreateInDto ratingCreateInDto,
                         UUID reviewerId) {
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        validateCompletedJob(job);

        User reviewer;
        User reviewee;
        RatingContext context;
        Application completedApplication = job.getCompletedApplication();
        User owner = job.getOwner();
        User worker = completedApplication.getApplicant();

        if (owner.getId().equals(reviewerId)) {
            reviewer = owner;
            reviewee = worker;
            context = RatingContext.WORKER;
        } else if (worker.getId().equals(reviewerId)) {
            reviewer = worker;
            reviewee = owner;
            context = RatingContext.JOB_OWNER;
        } else {
            throw new ForbiddenException("Only participants of the completed job can leave a rating");
        }

        if (reviewer.getId().equals(reviewee.getId())) {
            throw new ForbiddenException("Users cannot rate themselves");
        }

        if (ratingRepository.existsByJobIdAndReviewerId(jobId, reviewerId)) {
            throw new DuplicateResourceException("You have already rated this job");
        }

        Rating rating = new Rating();
        rating.setJob(job);
        rating.setReviewer(reviewer);
        rating.setReviewee(reviewee);
        rating.setContext(context);
        rating.setStars(ratingCreateInDto.stars());
        rating.setComment(normalizeComment(ratingCreateInDto.comment()));

        return ratingRepository.saveAndFlush(rating);
    }

    private void validateCompletedJob(Job job) {
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new ServiceException("Ratings can only be created for completed jobs");
        }

        if (job.getCompletedApplication() == null) {
            throw new ServiceException("Completed job does not have a completed application");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String normalizedComment = comment.trim();
        return normalizedComment.isEmpty() ? null : normalizedComment;
    }
}
