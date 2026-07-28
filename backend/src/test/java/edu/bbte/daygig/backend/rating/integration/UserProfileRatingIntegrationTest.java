package edu.bbte.daygig.backend.rating.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.RatingContext;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.Rating;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.RatingRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.security.JwtService;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileRatingIntegrationTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void profileSeparatesRatingSummariesAndCompletedJobCounts() throws Exception {
        User profileOwner = saveUser("rating-profile-owner");
        User firstJobOwner = saveUser("first-job-owner");
        User secondJobOwner = saveUser("second-job-owner");
        User workerForOwnedJob = saveUser("worker-for-owned-job");
        User viewer = saveUser("rating-profile-viewer");

        Job firstWorkerJob = saveCompletedJob(firstJobOwner, profileOwner, "First worker job");
        Job secondWorkerJob = saveCompletedJob(secondJobOwner, profileOwner, "Second worker job");
        Job ownedJob = saveCompletedJob(profileOwner, workerForOwnedJob, "Owned completed job");

        saveRating(firstWorkerJob, firstJobOwner, profileOwner, RatingContext.WORKER, 5);
        saveRating(secondWorkerJob, secondJobOwner, profileOwner, RatingContext.WORKER, 4);
        saveRating(ownedJob, workerForOwnedJob, profileOwner, RatingContext.JOB_OWNER, 3);

        mockMvc.perform(get("/users/{id}/profile", profileOwner.getId())
                .cookie(accessTokenCookie(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerRating.average").value(4.5))
                .andExpect(jsonPath("$.workerRating.count").value(2))
                .andExpect(jsonPath("$.jobOwnerRating.average").value(3.0))
                .andExpect(jsonPath("$.jobOwnerRating.count").value(1))
                .andExpect(jsonPath("$.completedJobsAsWorker").value(2))
                .andExpect(jsonPath("$.completedJobsAsOwner").value(1));
    }

    @Test
    void profileReturnsNullAveragesAndZeroCountsWithoutActivity() throws Exception {
        User profileOwner = saveUser("empty-rating-profile");
        User viewer = saveUser("empty-rating-viewer");

        mockMvc.perform(get("/users/{id}/profile", profileOwner.getId())
                .cookie(accessTokenCookie(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerRating.average").value(nullValue()))
                .andExpect(jsonPath("$.workerRating.count").value(0))
                .andExpect(jsonPath("$.jobOwnerRating.average").value(nullValue()))
                .andExpect(jsonPath("$.jobOwnerRating.count").value(0))
                .andExpect(jsonPath("$.completedJobsAsWorker").value(0))
                .andExpect(jsonPath("$.completedJobsAsOwner").value(0));
    }

    private Job saveCompletedJob(User owner,
                                 User worker,
                                 String title) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription("Profile rating integration test job");
        job.setCategory(JobCategory.CLEANING);
        job.setStatus(JobStatus.OPEN);
        job.setHourlyRate(new BigDecimal("45.00"));
        job.setCurrency(Currency.RON);
        job.setLocationName("Cluj-Napoca");
        job.setLatitude(new BigDecimal("46.770439"));
        job.setLongitude(new BigDecimal("23.591423"));
        job.setOwner(owner);
        jobRepository.saveAndFlush(job);

        Application application = new Application();
        application.setApplicant(worker);
        application.setJob(job);
        application.setStatus(ApplicationStatus.ACTIVE);
        applicationRepository.saveAndFlush(application);

        job.setCompletedApplication(application);
        job.setCompletedAt(LocalDateTime.now());
        job.setStatus(JobStatus.COMPLETED);
        return jobRepository.saveAndFlush(job);
    }

    private void saveRating(Job job,
                            User reviewer,
                            User reviewee,
                            RatingContext context,
                            int stars) {
        Rating rating = new Rating();
        rating.setJob(job);
        rating.setReviewer(reviewer);
        rating.setReviewee(reviewee);
        rating.setContext(context);
        rating.setStars(stars);
        ratingRepository.saveAndFlush(rating);
    }

    private User saveUser(String emailPrefix) {
        User user = new User();
        user.setEmail(emailPrefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("0712345678");
        user.setDeleted(false);
        user.setTermsAcceptedAt(LocalDateTime.now());
        return userRepository.saveAndFlush(user);
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
    }
}
