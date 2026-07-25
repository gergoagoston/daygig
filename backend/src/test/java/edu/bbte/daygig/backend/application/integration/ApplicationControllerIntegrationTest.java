package edu.bbte.daygig.backend.application.integration;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApplicationControllerIntegrationTest {

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
    void applyCreatesActiveApplicationForOpenJob() throws Exception {
        User owner = saveUser("apply-owner");
        User applicant = saveUser("apply-applicant");
        Job job = saveJob(owner, "Open apply job", JobStatus.OPEN);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.appliedAt").exists())
                .andExpect(jsonPath("$.job.id").value(job.getId().toString()))
                .andExpect(jsonPath("$.job.status").value("OPEN"))
                .andExpect(jsonPath("$.job.completedApplicationId").isEmpty());

        Application saved = applicationRepository
                .findAllByApplicantId(applicant.getId())
                .getFirst();
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.ACTIVE);
    }

    @Test
    void applyRejectsDuplicateApplication() throws Exception {
        User owner = saveUser("duplicate-owner");
        User applicant = saveUser("duplicate-applicant");
        Job job = saveJob(owner, "Duplicate apply job", JobStatus.OPEN);
        saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resource Conflict"));
    }

    @Test
    void applyRejectsClosedJob() throws Exception {
        User owner = saveUser("closed-owner");
        User applicant = saveUser("closed-applicant");
        Job job = saveJob(owner, "Closed apply job", JobStatus.COMPLETED);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot apply to a job that is not open"));
    }

    @Test
    void applyRejectsOwnJob() throws Exception {
        User owner = saveUser("own-job-owner");
        Job job = saveJob(owner, "Own apply job", JobStatus.OPEN);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot apply to your own job"));
    }

    @Test
    void getMineReturnsCurrentUsersApplications() throws Exception {
        User owner = saveUser("mine-owner");
        User applicant = saveUser("mine-applicant");
        User otherApplicant = saveUser("mine-other");
        Job openJob = saveJob(owner, "Mine open job", JobStatus.OPEN);
        Job completedJob = saveJob(owner, "Mine completed job", JobStatus.COMPLETED);
        saveApplication(applicant, openJob, ApplicationStatus.ACTIVE);
        Application completedApplication = saveApplication(applicant, completedJob, ApplicationStatus.ACTIVE);
        completedJob.setCompletedApplication(completedApplication);
        completedJob.setCompletedAt(LocalDateTime.now());
        jobRepository.saveAndFlush(completedJob);
        saveApplication(otherApplicant, openJob, ApplicationStatus.REJECTED);

        mockMvc.perform(get("/applications/mine")
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("ACTIVE", "ACTIVE")))
                .andExpect(jsonPath("$[*].job.status", containsInAnyOrder("OPEN", "COMPLETED")))
                .andExpect(jsonPath("$[?(@.job.status == 'COMPLETED')].job.completedApplicationId")
                        .value(completedApplication.getId().toString()));
    }

    @Test
    void getByJobReturnsApplicationsForOwner() throws Exception {
        User owner = saveUser("owner-list-owner");
        User applicant = saveUser("owner-list-applicant");
        Job job = saveJob(owner, "Owner list job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(get("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(application.getId().toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].applicant.id").value(applicant.getId().toString()));
    }

    @Test
    void ownerCanRejectActiveApplication() throws Exception {
        User owner = saveUser("reject-owner");
        User applicant = saveUser("reject-applicant");
        Job job = saveJob(owner, "Reject job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isNoContent());

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void applicantCanWithdrawActiveApplication() throws Exception {
        User owner = saveUser("withdraw-owner");
        User applicant = saveUser("withdraw-applicant");
        Job job = saveJob(owner, "Withdraw job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(applicant))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.WITHDRAWN)))
                .andExpect(status().isNoContent());

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void nonOwnerCannotRejectApplication() throws Exception {
        User owner = saveUser("forbidden-reject-owner");
        User applicant = saveUser("forbidden-reject-applicant");
        User otherUser = saveUser("forbidden-reject-other");
        Job job = saveJob(owner, "Forbidden reject job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonApplicantCannotWithdrawApplication() throws Exception {
        User owner = saveUser("forbidden-withdraw-owner");
        User applicant = saveUser("forbidden-withdraw-applicant");
        User otherUser = saveUser("forbidden-withdraw-other");
        Job job = saveJob(owner, "Forbidden withdraw job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.WITHDRAWN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void closedJobApplicationsCannotBeUpdated() throws Exception {
        User owner = saveUser("closed-update-owner");
        User applicant = saveUser("closed-update-applicant");
        Job job = saveJob(owner, "Closed update job", JobStatus.COMPLETED);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Applications can only be updated for open jobs"));
    }

    @Test
    void inactiveApplicationCannotBeUpdatedAgain() throws Exception {
        User owner = saveUser("inactive-owner");
        User applicant = saveUser("inactive-applicant");
        Job job = saveJob(owner, "Inactive application job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.REJECTED);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only active applications can be updated"));
    }

    @Test
    void activeStatusCannotBeRequested() throws Exception {
        User owner = saveUser("active-status-owner");
        User applicant = saveUser("active-status-applicant");
        Job job = saveJob(owner, "Active status job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACTIVE);

        mockMvc.perform(patch("/applications/{applicationId}", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.ACTIVE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Application status can only be changed to REJECTED or WITHDRAWN"));
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
    }

    private String statusUpdateJson(ApplicationStatus status) {
        return """
                {
                  "status": "%s"
                }
                """.formatted(status);
    }

    private User saveUser(String emailPrefix) {
        User user = new User();
        user.setEmail(emailPrefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
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
        job.setDescription("Application integration test job");
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
}
