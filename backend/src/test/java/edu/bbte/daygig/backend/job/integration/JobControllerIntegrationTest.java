package edu.bbte.daygig.backend.job.integration;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobControllerIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getJobsWorksWithoutAuthenticationAndReturnsOnlyOpenJobs() throws Exception {
        User owner = saveUser("public-list-owner");
        Job openJob = saveJob(owner, "Open job", JobCategory.CLEANING, JobStatus.OPEN);
        saveJob(owner, "Assigned job", JobCategory.MOVING, JobStatus.ASSIGNED);
        saveJob(owner, "Completed job", JobCategory.GARDENING, JobStatus.COMPLETED);
        saveJob(owner, "Cancelled job", JobCategory.DELIVERY, JobStatus.CANCELLED);

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(openJob.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value(openJob.getTitle()))
                .andExpect(jsonPath("$.content[0].category").value("CLEANING"))
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
    void getJobDetailsWorksWithoutAuthentication() throws Exception {
        User owner = saveUser("details-owner");
        Job job = saveJob(owner, "Details job", JobCategory.EVENT_HELP, JobStatus.ASSIGNED);

        mockMvc.perform(get("/jobs/{id}", job.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(job.getId().toString()))
                .andExpect(jsonPath("$.title").value(job.getTitle()))
                .andExpect(jsonPath("$.description").value(job.getDescription()))
                .andExpect(jsonPath("$.category").value("EVENT_HELP"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.hourlyRate").value(45.0))
                .andExpect(jsonPath("$.currency").value("RON"))
                .andExpect(jsonPath("$.locationName").value("Cluj-Napoca"))
                .andExpect(jsonPath("$.latitude").value(46.770439))
                .andExpect(jsonPath("$.longitude").value(23.591423));
    }

    @Test
    void getMissingJobDetailsReturnsNotFound() throws Exception {
        mockMvc.perform(get("/jobs/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
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
}
