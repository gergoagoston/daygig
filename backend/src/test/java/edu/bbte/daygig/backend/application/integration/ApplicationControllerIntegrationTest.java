package edu.bbte.daygig.backend.application.integration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
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
