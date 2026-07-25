package edu.bbte.daygig.backend.job.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.security.JwtService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobControllerIntegrationTest {

    private static final String PASSWORD = "password123";
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void getJobsWorksWithoutAuthenticationAndReturnsOnlyOpenJobs() throws Exception {
        User owner = saveUser("public-list-owner");
        Job openJob = saveJob(owner, "Open job", JobCategory.CLEANING, JobStatus.OPEN);
        saveJob(owner, "Completed job", JobCategory.GARDENING, JobStatus.COMPLETED);
        saveJob(owner, "Cancelled job", JobCategory.DELIVERY, JobStatus.CANCELLED);

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(openJob.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value(openJob.getTitle()))
                .andExpect(jsonPath("$.content[0].category").value("CLEANING"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.content[0].currency").value("RON"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getJobsSupportsPaginationWithoutAuthentication() throws Exception {
        User owner = saveUser("pagination-owner");
        saveJob(owner, "First open job", JobCategory.CLEANING, JobStatus.OPEN);
        saveJob(owner, "Second open job", JobCategory.MOVING, JobStatus.OPEN);
        saveJob(owner, "Third open job", JobCategory.GARDENING, JobStatus.OPEN);

        mockMvc.perform(get("/jobs")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void getMineReturnsOnlyCurrentUsersJobs() throws Exception {
        User owner = saveUser("mine-owner");
        User otherOwner = saveUser("mine-other-owner");
        Job firstOwnJob = saveJob(owner, "Own open job", JobCategory.CLEANING, JobStatus.OPEN);
        Job secondOwnJob = saveJob(owner, "Own completed job", JobCategory.GARDENING, JobStatus.COMPLETED);
        saveJob(otherOwner, "Other owner job", JobCategory.MOVING, JobStatus.OPEN);

        mockMvc.perform(get("/jobs/mine")
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(firstOwnJob.getId().toString(),
                        secondOwnJob.getId().toString())))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(firstOwnJob.getTitle(),
                        secondOwnJob.getTitle())))
                .andExpect(jsonPath("$[*].category", containsInAnyOrder("CLEANING", "GARDENING")))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("OPEN", "COMPLETED")))
                .andExpect(jsonPath("$[*].locationName", containsInAnyOrder("Cluj-Napoca", "Cluj-Napoca")))
                .andExpect(jsonPath("$[*].currency", containsInAnyOrder("RON", "RON")));
    }

    @Test
    void getMineRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/jobs/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createJobCreatesOpenJobForAuthenticatedUser() throws Exception {
        User owner = saveUser("create-owner");

        String response = mockMvc.perform(post("/jobs")
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJobJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Created job"))
                .andExpect(jsonPath("$.description").value("Created job description"))
                .andExpect(jsonPath("$.category").value("DELIVERY"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.hourlyRate").value(60.0))
                .andExpect(jsonPath("$.currency").value("RON"))
                .andExpect(jsonPath("$.locationName").value("Oradea"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdJobId = UUID.fromString(response.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
        Job createdJob = jobRepository.findById(createdJobId).orElseThrow();

        assertThat(createdJob.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(createdJob.getStatus()).isEqualTo(JobStatus.OPEN);
        assertThat(createdJob.getCurrency()).isEqualTo(Currency.RON);
        assertThat(createdJob.getLatitude()).isEqualByComparingTo(new BigDecimal("47.046500"));
        assertThat(createdJob.getLongitude()).isEqualByComparingTo(new BigDecimal("21.918900"));
    }

    @Test
    void createJobRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJobJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createJobRejectsValidationErrors() throws Exception {
        User owner = saveUser("create-validation-owner");

        mockMvc.perform(post("/jobs")
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "",
                          "description": "",
                          "category": null,
                          "hourlyRate": -5,
                          "locationName": "",
                          "latitude": 95,
                          "longitude": -190
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.title").exists())
                .andExpect(jsonPath("$.details.description").exists())
                .andExpect(jsonPath("$.details.category").exists())
                .andExpect(jsonPath("$.details.hourlyRate").exists())
                .andExpect(jsonPath("$.details.locationName").exists())
                .andExpect(jsonPath("$.details.latitude").exists())
                .andExpect(jsonPath("$.details.longitude").exists());
    }

    @Test
    void getJobDetailsWorksWithoutAuthentication() throws Exception {
        User owner = saveUser("details-owner");
        Job job = saveJob(owner, "Details job", JobCategory.EVENT_HELP, JobStatus.OPEN);

        mockMvc.perform(get("/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(job.getId().toString()))
                .andExpect(jsonPath("$.title").value(job.getTitle()))
                .andExpect(jsonPath("$.description").value(job.getDescription()))
                .andExpect(jsonPath("$.category").value("EVENT_HELP"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.hourlyRate").value(45.0))
                .andExpect(jsonPath("$.currency").value("RON"))
                .andExpect(jsonPath("$.locationName").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.latitude").doesNotExist())
                .andExpect(jsonPath("$.longitude").doesNotExist())
                .andExpect(jsonPath("$.approximateLatitude").value(46.77))
                .andExpect(jsonPath("$.approximateLongitude").value(23.59))
                .andExpect(jsonPath("$.locationRadiusMeters").value(1000))
                .andExpect(jsonPath("$.owner.id").value(owner.getId().toString()))
                .andExpect(jsonPath("$.owner.firstName").value(owner.getFirstName()))
                .andExpect(jsonPath("$.owner.lastName").value(owner.getLastName()));
    }

    @Test
    void getMissingJobDetailsReturnsNotFound() throws Exception {
        mockMvc.perform(get("/jobs/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    void ownerCanCompleteJobWithActiveApplication() throws Exception {
        User owner = saveUser("complete-owner");
        User applicant = saveUser("complete-applicant");
        User otherApplicant = saveUser("complete-other-applicant");
        Job job = saveJob(owner, "Completable job", JobCategory.CLEANING, JobStatus.OPEN);
        Application completedApplication = saveApplication(applicant, job, ApplicationStatus.ACTIVE);
        Application otherApplication = saveApplication(otherApplicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(completedApplication.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedApplicationId")
                        .value(completedApplication.getId().toString()))
                .andExpect(jsonPath("$.completedAt").exists());

        Job completedJob = jobRepository.findById(job.getId()).orElseThrow();
        assertThat(completedJob.getCompletedApplication().getId()).isEqualTo(completedApplication.getId());
        assertThat(completedJob.getCompletedAt()).isNotNull();
        assertThat(completedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(applicationRepository.findById(completedApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.ACTIVE);
        assertThat(applicationRepository.findById(otherApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.ACTIVE);
    }

    @Test
    void completingJobWithSameApplicationIsIdempotent() throws Exception {
        User owner = saveUser("idempotent-owner");
        User applicant = saveUser("idempotent-applicant");
        Job job = saveJob(owner, "Idempotent job", JobCategory.CLEANING, JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(application.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(application.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedApplicationId").value(application.getId().toString()));
    }

    @Test
    void completionRejectsApplicationFromAnotherJob() throws Exception {
        User owner = saveUser("wrong-job-owner");
        User applicant = saveUser("wrong-job-applicant");
        Job job = saveJob(owner, "Target job", JobCategory.CLEANING, JobStatus.OPEN);
        Job otherJob = saveJob(owner, "Other job", JobCategory.MOVING, JobStatus.OPEN);
        Application application = saveApplication(applicant, otherJob, ApplicationStatus.ACTIVE);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(application.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Application does not belong to this job"));
    }

    @Test
    void completionRejectsInactiveApplication() throws Exception {
        User owner = saveUser("inactive-completion-owner");
        User applicant = saveUser("inactive-completion-applicant");
        Job job = saveJob(owner, "Inactive completion job", JobCategory.CLEANING, JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.REJECTED);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(application.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only active applications can complete a job"));
    }

    @Test
    void completionRejectsNonOwner() throws Exception {
        User owner = saveUser("forbidden-completion-owner");
        User applicant = saveUser("forbidden-completion-applicant");
        User otherUser = saveUser("forbidden-completion-other");
        Job job = saveJob(owner, "Forbidden completion job", JobCategory.CLEANING, JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(application.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void secondDifferentCompletionReturnsConflict() throws Exception {
        User owner = saveUser("conflict-completion-owner");
        User firstApplicant = saveUser("conflict-completion-first");
        User secondApplicant = saveUser("conflict-completion-second");
        Job job = saveJob(owner, "Conflict completion job", JobCategory.CLEANING, JobStatus.OPEN);
        Application firstApplication = saveApplication(firstApplicant, job, ApplicationStatus.ACTIVE);
        Application secondApplication = saveApplication(secondApplicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(firstApplication.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/jobs/{jobId}/completion", job.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionJson(secondApplication.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resource Conflict"));
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
    }

    private String validCreateJobJson() {
        return """
                {
                  "title": "Created job",
                  "description": "Created job description",
                  "category": "DELIVERY",
                  "hourlyRate": 60.00,
                  "locationName": "Oradea",
                  "latitude": 47.046500,
                  "longitude": 21.918900
                }
                """;
    }

    private String completionJson(UUID applicationId) {
        return """
                {
                  "applicationId": "%s"
                }
                """.formatted(applicationId);
    }

    private User saveUser(String emailPrefix) {
        User user = new User();
        user.setEmail(emailPrefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("Owner");
        user.setPhoneNumber("0712345678");
        user.setDeleted(false);
        user.setTermsAcceptedAt(LocalDateTime.now());

        return userRepository.saveAndFlush(user);
    }

    private Job saveJob(User owner,
                        String title,
                        JobCategory category,
                        JobStatus status) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription("Public job test description");
        job.setCategory(category);
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
}
