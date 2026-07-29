package edu.bbte.daygig.backend.service.user;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.rating.RatingSummaryOutDto;
import edu.bbte.daygig.backend.dto.user.UserProfileOutDto;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.RatingContext;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.service.avatar.UserAvatarService;
import edu.bbte.daygig.backend.service.rating.RatingService;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserService userService;
    private final UserAvatarService userAvatarService;
    private final RatingService ratingService;
    private final JobRepository jobRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileOutDto getProfile(UUID userId) {
        User user = userService.getById(userId);
        RatingSummaryOutDto workerRating = ratingService.getSummary(userId, RatingContext.WORKER);
        RatingSummaryOutDto jobOwnerRating = ratingService.getSummary(userId, RatingContext.JOB_OWNER);
        long completedJobsAsWorker = jobRepository
                .countByCompletedApplicationApplicantIdAndStatus(userId, JobStatus.COMPLETED);
        long completedJobsAsOwner = jobRepository.countByOwnerIdAndStatus(userId, JobStatus.COMPLETED);

        return userMapper.toProfileDto(user, userAvatarService.getAvatarUrl(userId), workerRating,
                jobOwnerRating, completedJobsAsWorker, completedJobsAsOwner);
    }
}
