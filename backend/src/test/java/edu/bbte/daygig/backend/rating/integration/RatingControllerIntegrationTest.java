package edu.bbte.daygig.backend.rating.integration;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RatingControllerIntegrationTest {

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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtService jwtService;

    @Test
    void ownerCanRateCompletedWorker() throws Exception {
        CompletedJob completedJob = saveCompletedJob("owner-rates-worker");

        mockMvc.perform(post("/jobs/{jobId}/ratings", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.owner()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(5, "  Careful and reliable work.  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(completedJob.job().getId().toString()))
                .andExpect(jsonPath("$.reviewer.id").value(completedJob.owner().getId().toString()))
                .andExpect(jsonPath("$.context").value("WORKER"))
                .andExpect(jsonPath("$.stars").value(5))
                .andExpect(jsonPath("$.comment").value("Careful and reliable work."));

        Rating rating = ratingRepository.findAll().getFirst();
        assertThat(rating.getReviewer().getId()).isEqualTo(completedJob.owner().getId());
        assertThat(rating.getReviewee().getId()).isEqualTo(completedJob.worker().getId());
        assertThat(rating.getContext()).isEqualTo(RatingContext.WORKER);
    }

    @Test
    void completedWorkerCanRateJobOwner() throws Exception {
        CompletedJob completedJob = saveCompletedJob("worker-rates-owner");

        mockMvc.perform(post("/jobs/{jobId}/ratings", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.worker()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(4, "Clear instructions.")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewer.id").value(completedJob.worker().getId().toString()))
                .andExpect(jsonPath("$.context").value("JOB_OWNER"))
                .andExpect(jsonPath("$.stars").value(4));
    }

    @Test
    void eligibilityIdentifiesOtherParticipantAndExistingRating() throws Exception {
        CompletedJob completedJob = saveCompletedJob("rating-eligibility");

        mockMvc.perform(get("/jobs/{jobId}/ratings/me", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.owner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.alreadyRated").value(false))
                .andExpect(jsonPath("$.reviewee.id").value(completedJob.worker().getId().toString()))
                .andExpect(jsonPath("$.context").value("WORKER"));

        createRating(completedJob.job(), completedJob.owner(), 5)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/jobs/{jobId}/ratings/me", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.owner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.alreadyRated").value(true));
    }

    @Test
    void eligibilityIsFalseForOpenJobAndUnrelatedUser() throws Exception {
        User owner = saveUser("ineligible-owner");
        User unrelatedUser = saveUser("ineligible-user");
        Job openJob = saveJob(owner, "Ineligible open job", JobStatus.OPEN);

        mockMvc.perform(get("/jobs/{jobId}/ratings/me", openJob.getId())
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.alreadyRated").value(false))
                .andExpect(jsonPath("$.reviewee").value(nullValue()))
                .andExpect(jsonPath("$.context").value(nullValue()));

        CompletedJob completedJob = saveCompletedJob("ineligible-unrelated");
        mockMvc.perform(get("/jobs/{jobId}/ratings/me", completedJob.job().getId())
                .cookie(accessTokenCookie(unrelatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.alreadyRated").value(false));
    }

    @Test
    void receivedRatingsAreFilteredByContextAndExposeOnlyPublicReviewerData() throws Exception {
        CompletedJob completedJob = saveCompletedJob("received-ratings");
        createRating(completedJob.job(), completedJob.owner(), 5)
                .andExpect(status().isCreated());
        createRating(completedJob.job(), completedJob.worker(), 4)
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/{userId}/ratings", completedJob.worker().getId())
                .param("context", "WORKER")
                .cookie(accessTokenCookie(completedJob.owner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].context").value("WORKER"))
                .andExpect(jsonPath("$[0].reviewer.id").value(completedJob.owner().getId().toString()))
                .andExpect(jsonPath("$[0].reviewer.firstName").value(completedJob.owner().getFirstName()))
                .andExpect(jsonPath("$[0].reviewer.email").doesNotExist())
                .andExpect(jsonPath("$[0].reviewer.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$[0].reviewer.role").doesNotExist())
                .andExpect(jsonPath("$[0].jobId").value(completedJob.job().getId().toString()))
                .andExpect(jsonPath("$[0].jobTitle").value(completedJob.job().getTitle()));

        mockMvc.perform(get("/users/{userId}/ratings", completedJob.worker().getId())
                .param("context", "JOB_OWNER")
                .cookie(accessTokenCookie(completedJob.owner())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void receivedRatingsRequireAuthenticationAndExistingUser() throws Exception {
        User viewer = saveUser("ratings-list-viewer");

        mockMvc.perform(get("/users/{userId}/ratings", viewer.getId())
                .param("context", "WORKER"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/{userId}/ratings", UUID.randomUUID())
                .param("context", "WORKER")
                .cookie(accessTokenCookie(viewer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void receivedRatingsAreOrderedWithNewestFirst() throws Exception {
        User worker = saveUser("ordered-ratings-worker");
        User firstOwner = saveUser("ordered-ratings-first-owner");
        User secondOwner = saveUser("ordered-ratings-second-owner");
        Job firstJob = saveCompletedJob(firstOwner, worker, "First ordered rating job");
        Job secondJob = saveCompletedJob(secondOwner, worker, "Second ordered rating job");
        Rating firstRating = saveRating(firstJob, firstOwner, worker, 4);
        Rating secondRating = saveRating(secondJob, secondOwner, worker, 5);

        setCreatedAt(firstRating, LocalDateTime.of(2026, 1, 1, 10, 0));
        setCreatedAt(secondRating, LocalDateTime.of(2026, 1, 2, 10, 0));
        entityManager.clear();

        mockMvc.perform(get("/users/{userId}/ratings", worker.getId())
                .param("context", "WORKER")
                .cookie(accessTokenCookie(firstOwner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(secondRating.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(firstRating.getId().toString()));
    }

    @Test
    void duplicateRatingReturnsConflict() throws Exception {
        CompletedJob completedJob = saveCompletedJob("duplicate-rating");

        createRating(completedJob.job(), completedJob.owner(), 5)
                .andExpect(status().isCreated());

        createRating(completedJob.job(), completedJob.owner(), 4)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resource Conflict"))
                .andExpect(jsonPath("$.message").value("You have already rated this job"));

        assertThat(ratingRepository.count()).isEqualTo(1);
    }

    @Test
    void nonSelectedApplicantCannotRateJobOwner() throws Exception {
        CompletedJob completedJob = saveCompletedJob("non-selected-applicant");
        User rejectedApplicant = saveUser("rejected-applicant");
        saveApplication(rejectedApplicant, completedJob.job(), ApplicationStatus.REJECTED);

        createRating(completedJob.job(), rejectedApplicant, 5)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Only participants of the completed job can leave a rating"));

        assertThat(ratingRepository.count()).isZero();
    }

    @Test
    void openJobCannotBeRated() throws Exception {
        User owner = saveUser("open-job-owner");
        Job job = saveJob(owner, "Open job", JobStatus.OPEN);

        createRating(job, owner, 5)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ratings can only be created for completed jobs"));
    }

    @Test
    void completedJobWithoutCompletedApplicationCannotBeRated() throws Exception {
        User owner = saveUser("missing-completion-owner");
        Job job = saveJob(owner, "Missing completed application", JobStatus.COMPLETED);

        createRating(job, owner, 5)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Completed job does not have a completed application"));
    }

    @Test
    void userCannotRateThemselves() throws Exception {
        User owner = saveUser("self-rating-owner");
        Job job = saveJob(owner, "Self rating job", JobStatus.OPEN);
        Application application = saveApplication(owner, job, ApplicationStatus.ACTIVE);
        completeJob(job, application);

        createRating(job, owner, 5)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Users cannot rate themselves"));
    }

    @Test
    void ratingRequestValidatesStarsAndComment() throws Exception {
        CompletedJob completedJob = saveCompletedJob("rating-validation");

        mockMvc.perform(post("/jobs/{jobId}/ratings", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.owner()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(0, "Invalid stars")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.stars").exists());

        mockMvc.perform(post("/jobs/{jobId}/ratings", completedJob.job().getId())
                .cookie(accessTokenCookie(completedJob.owner()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(5, "x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.comment").exists());

        assertThat(ratingRepository.count()).isZero();
    }

    @Test
    void ratingRequiresAuthentication() throws Exception {
        CompletedJob completedJob = saveCompletedJob("rating-authentication");

        mockMvc.perform(post("/jobs/{jobId}/ratings", completedJob.job().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(5, "Good work")))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions createRating(Job job,
                                                                            User reviewer,
                                                                            int stars)
            throws Exception {
        return mockMvc.perform(post("/jobs/{jobId}/ratings", job.getId())
                .cookie(accessTokenCookie(reviewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ratingJson(stars, null)));
    }

    private String ratingJson(int stars,
                              String comment) {
        String commentJson = comment == null ? "null" : "\"" + comment + "\"";
        return """
                {
                  "stars": %d,
                  "comment": %s
                }
                """.formatted(stars, commentJson);
    }

    private CompletedJob saveCompletedJob(String prefix) {
        User owner = saveUser(prefix + "-owner");
        User worker = saveUser(prefix + "-worker");
        Job job = saveCompletedJob(owner, worker, "Completed job " + prefix);
        return new CompletedJob(owner, worker, job);
    }

    private Job saveCompletedJob(User owner,
                                 User worker,
                                 String title) {
        Job job = saveJob(owner, title, JobStatus.OPEN);
        Application application = saveApplication(worker, job, ApplicationStatus.ACTIVE);
        completeJob(job, application);
        return job;
    }

    private Rating saveRating(Job job,
                              User reviewer,
                              User reviewee,
                              int stars) {
        Rating rating = new Rating();
        rating.setJob(job);
        rating.setReviewer(reviewer);
        rating.setReviewee(reviewee);
        rating.setContext(RatingContext.WORKER);
        rating.setStars(stars);
        return ratingRepository.saveAndFlush(rating);
    }

    private void setCreatedAt(Rating rating,
                              LocalDateTime createdAt) {
        jdbcTemplate.update("UPDATE ratings SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt), rating.getId());
    }

    private void completeJob(Job job,
                             Application application) {
        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedApplication(application);
        job.setCompletedAt(LocalDateTime.now());
        jobRepository.saveAndFlush(job);
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

    private Job saveJob(User owner,
                        String title,
                        JobStatus status) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription("Rating integration test job");
        job.setCategory(JobCategory.CLEANING);
        job.setStatus(status);
        job.setHourlyRate(new BigDecimal("45.00"));
        job.setCurrency(Currency.RON);
        job.setLocationName("Cluj-Napoca");
        job.setLatitude(new BigDecimal("46.770439"));
        job.setLongitude(new BigDecimal("23.591423"));
        job.setOwner(owner);
        return jobRepository.saveAndFlush(job);
    }

    private Application saveApplication(User applicant,
                                        Job job,
                                        ApplicationStatus status) {
        Application application = new Application();
        application.setApplicant(applicant);
        application.setJob(job);
        application.setStatus(status);
        return applicationRepository.saveAndFlush(application);
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
    }

    private record CompletedJob(User owner, User worker, Job job) {
    }
}
