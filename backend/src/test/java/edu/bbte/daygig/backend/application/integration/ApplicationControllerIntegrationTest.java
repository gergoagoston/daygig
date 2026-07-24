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
    void applyCreatesPendingApplicationForOpenJob() throws Exception {
        User owner = saveUser("apply-owner");
        User applicant = saveUser("apply-applicant");
        Job job = saveJob(owner, "Open apply job", JobStatus.OPEN);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.appliedAt").exists())
                .andExpect(jsonPath("$.job.id").value(job.getId().toString()))
                .andExpect(jsonPath("$.job.title").value(job.getTitle()))
                .andExpect(jsonPath("$.job.category").value("CLEANING"))
                .andExpect(jsonPath("$.job.locationName").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.job.hourlyRate").value(45.0))
                .andExpect(jsonPath("$.job.currency").value("RON"))
                .andExpect(jsonPath("$.job.status").value("OPEN"));

        assertThat(applicationRepository.existsByApplicantIdAndJobId(applicant.getId(), job.getId())).isTrue();
    }

    @Test
    void applyRejectsUnauthenticatedRequest() throws Exception {
        User owner = saveUser("unauthorized-owner");
        Job job = saveJob(owner, "Unauthorized apply job", JobStatus.OPEN);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applyRejectsDuplicateApplication() throws Exception {
        User owner = saveUser("duplicate-owner");
        User applicant = saveUser("duplicate-applicant");
        Job job = saveJob(owner, "Duplicate apply job", JobStatus.OPEN);
        saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resource Conflict"))
                .andExpect(jsonPath("$.message").value("You have already applied to this job"));
    }

    @Test
    void applyRejectsMissingJob() throws Exception {
        User applicant = saveUser("missing-job-applicant");

        mockMvc.perform(post("/jobs/{jobId}/applications", UUID.randomUUID())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    void applyRejectsNonOpenJob() throws Exception {
        User owner = saveUser("assigned-owner");
        User applicant = saveUser("assigned-applicant");
        Job job = saveJob(owner, "Assigned apply job", JobStatus.ASSIGNED);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Logic Error"))
                .andExpect(jsonPath("$.message").value("Cannot apply to a job that is not open"));
    }

    @Test
    void applyRejectsOwnPostedJob() throws Exception {
        User owner = saveUser("own-job-owner");
        Job job = saveJob(owner, "Own apply job", JobStatus.OPEN);

        mockMvc.perform(post("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Logic Error"))
                .andExpect(jsonPath("$.message").value("Cannot apply to your own job"));
    }

    @Test
    void getMineReturnsCurrentUserApplicationsWithJobSummary() throws Exception {
        User owner = saveUser("mine-owner");
        User applicant = saveUser("mine-applicant");
        User otherApplicant = saveUser("mine-other-applicant");
        Job firstJob = saveJob(owner, "Mine first job", JobStatus.OPEN);
        Job secondJob = saveJob(owner, "Mine second job", JobStatus.ASSIGNED);
        Job otherJob = saveJob(owner, "Other user job", JobStatus.OPEN);
        saveApplication(applicant, firstJob, ApplicationStatus.PENDING);
        saveApplication(applicant, secondJob, ApplicationStatus.ACCEPTED);
        saveApplication(otherApplicant, otherJob, ApplicationStatus.REJECTED);

        mockMvc.perform(get("/applications/mine")
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", containsInAnyOrder("PENDING", "ACCEPTED")))
                .andExpect(jsonPath("$[*].job.title", containsInAnyOrder(firstJob.getTitle(), secondJob.getTitle())))
                .andExpect(jsonPath("$[*].job.locationName", containsInAnyOrder("Cluj-Napoca", "Cluj-Napoca")))
                .andExpect(jsonPath("$[*].job.currency", containsInAnyOrder("RON", "RON")))
                .andExpect(jsonPath("$[*].job.status", containsInAnyOrder("OPEN", "ASSIGNED")));
    }

    @Test
    void getByJobReturnsApplicationsForJobOwner() throws Exception {
        User owner = saveUser("owner-list-owner");
        User applicant = saveUser("owner-list-applicant");
        Job job = saveJob(owner, "Owner application list job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(get("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(application.getId().toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].appliedAt").exists())
                .andExpect(jsonPath("$[0].applicant.id").value(applicant.getId().toString()))
                .andExpect(jsonPath("$[0].applicant.firstName").value(applicant.getFirstName()))
                .andExpect(jsonPath("$[0].applicant.lastName").value(applicant.getLastName()));
    }

    @Test
    void getByJobRejectsNonOwner() throws Exception {
        User owner = saveUser("owner-list-forbidden-owner");
        User otherUser = saveUser("owner-list-forbidden-other");
        Job job = saveJob(owner, "Owner application forbidden job", JobStatus.OPEN);

        mockMvc.perform(get("/jobs/{jobId}/applications", job.getId())
                .cookie(accessTokenCookie(otherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void updateStatusAcceptsPendingApplicationAndAssignsJob() throws Exception {
        User owner = saveUser("accept-owner");
        User acceptedApplicant = saveUser("accept-applicant");
        User pendingApplicant = saveUser("accept-other-applicant");
        Job job = saveJob(owner, "Accept job", JobStatus.OPEN);
        Application acceptedApplication = saveApplication(acceptedApplicant, job, ApplicationStatus.PENDING);
        Application pendingApplication = saveApplication(pendingApplicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", acceptedApplication.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.ACCEPTED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(acceptedApplication.getId().toString()))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.applicant.id").value(acceptedApplicant.getId().toString()));

        assertThat(applicationRepository.findById(acceptedApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(applicationRepository.findById(pendingApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.PENDING);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.ASSIGNED);
    }

    @Test
    void updateStatusAcceptsMultipleApplicationsForAssignedJob() throws Exception {
        User owner = saveUser("multi-accept-owner");
        User acceptedApplicant = saveUser("multi-accept-current");
        User nextApplicant = saveUser("multi-accept-next");
        Job job = saveJob(owner, "Multiple accept job", JobStatus.ASSIGNED);
        Application acceptedApplication = saveApplication(acceptedApplicant, job, ApplicationStatus.ACCEPTED);
        Application nextApplication = saveApplication(nextApplicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", nextApplication.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.ACCEPTED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        assertThat(applicationRepository.findById(acceptedApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(applicationRepository.findById(nextApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.ASSIGNED);
    }

    @Test
    void updateStatusRejectsPendingApplicationWithoutChangingJobStatus() throws Exception {
        User owner = saveUser("reject-owner");
        User applicant = saveUser("reject-applicant");
        Job job = saveJob(owner, "Reject job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void updateStatusRejectsAcceptedApplication() throws Exception {
        User owner = saveUser("reject-accepted-owner");
        User applicant = saveUser("reject-accepted-applicant");
        Job job = saveJob(owner, "Reject accepted job", JobStatus.ASSIGNED);
        Application application = saveApplication(applicant, job, ApplicationStatus.ACCEPTED);

        mockMvc.perform(patch("/applications/{applicationId}/status", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.ASSIGNED);
    }

    @Test
    void updateStatusRejectsNonOwner() throws Exception {
        User owner = saveUser("application-update-owner");
        User otherUser = saveUser("application-update-other");
        User applicant = saveUser("application-update-applicant");
        Job job = saveJob(owner, "Application update forbidden job", JobStatus.OPEN);
        Application application = saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", application.getId())
                .cookie(accessTokenCookie(otherUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void updateStatusRejectsCompletedJobApplications() throws Exception {
        User owner = saveUser("completed-update-owner");
        User applicant = saveUser("completed-update-applicant");
        Job job = saveJob(owner, "Completed update job", JobStatus.COMPLETED);
        Application application = saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.REJECTED)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Logic Error"))
                .andExpect(jsonPath("$.message").value("Cannot update applications for a completed job"));
    }

    @Test
    void updateStatusCompletesAcceptedApplicationAndCompletesJob() throws Exception {
        User owner = saveUser("complete-application-owner");
        User completedApplicant = saveUser("complete-application-worker");
        User pendingApplicant = saveUser("complete-application-pending");
        User acceptedApplicant = saveUser("complete-application-accepted");
        Job job = saveJob(owner, "Complete application job", JobStatus.ASSIGNED);
        Application completedApplication = saveApplication(completedApplicant, job, ApplicationStatus.ACCEPTED);
        Application pendingApplication = saveApplication(pendingApplicant, job, ApplicationStatus.PENDING);
        Application acceptedApplication = saveApplication(acceptedApplicant, job, ApplicationStatus.ACCEPTED);

        mockMvc.perform(patch("/applications/{applicationId}/status", completedApplication.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.COMPLETED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(completedApplication.getId().toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.applicant.id").value(completedApplicant.getId().toString()));

        assertThat(applicationRepository.findById(completedApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.COMPLETED);
        assertThat(applicationRepository.findById(pendingApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(applicationRepository.findById(acceptedApplication.getId()).orElseThrow().getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(jobRepository.findById(job.getId()).orElseThrow().getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void updateStatusRejectsCompletingPendingApplication() throws Exception {
        User owner = saveUser("complete-pending-owner");
        User applicant = saveUser("complete-pending-applicant");
        Job job = saveJob(owner, "Complete pending job", JobStatus.ASSIGNED);
        Application application = saveApplication(applicant, job, ApplicationStatus.PENDING);

        mockMvc.perform(patch("/applications/{applicationId}/status", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJson(ApplicationStatus.COMPLETED)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business Logic Error"))
                .andExpect(jsonPath("$.message").value("Only accepted applications can be completed"));
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
