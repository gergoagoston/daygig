package edu.bbte.daygig.backend.conversation.integration;

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
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.Conversation;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.repository.ConversationRepository;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.MessageRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.security.JwtService;
import edu.bbte.daygig.backend.service.conversation.ConversationService;
import edu.bbte.daygig.backend.service.conversation.MessageSendResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConversationControllerIntegrationTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ConversationService conversationService;

    @Test
    void ownerCreatesConversationWithFirstMessage() throws Exception {
        User owner = saveUser("create-owner");
        User applicant = saveUser("create-applicant");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.ACTIVE);

        mockMvc.perform(post("/applications/{applicationId}/conversation", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(messageJson("  Hello!  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(application.getId().toString()))
                .andExpect(jsonPath("$.otherParticipant.id").value(applicant.getId().toString()))
                .andExpect(jsonPath("$.lastMessage").value("Hello!"));

        assertThat(conversationRepository.count()).isOne();
        assertThat(messageRepository.findAll()).singleElement().satisfies(message -> {
            assertThat(message.getContent()).isEqualTo("Hello!");
            assertThat(message.getSender().getId()).isEqualTo(owner.getId());
        });
    }

    @Test
    void applicantCannotStartConversation() throws Exception {
        User owner = saveUser("forbidden-owner");
        User applicant = saveUser("forbidden-applicant");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.ACTIVE);

        mockMvc.perform(post("/applications/{applicationId}/conversation", application.getId())
                .cookie(accessTokenCookie(applicant))
                .contentType(MediaType.APPLICATION_JSON)
                .content(messageJson("Hello")))
                .andExpect(status().isForbidden());

        assertThat(conversationRepository.count()).isZero();
        assertThat(messageRepository.count()).isZero();
    }

    @Test
    void conversationCanOnlyBeStartedForActiveApplication() throws Exception {
        User owner = saveUser("inactive-owner");
        User applicant = saveUser("inactive-applicant");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.REJECTED);

        mockMvc.perform(post("/applications/{applicationId}/conversation", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(messageJson("Hello")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A conversation can only be started for an active application"));
    }

    @Test
    void participantsCanAccessConversationButOtherUsersCannot() throws Exception {
        User owner = saveUser("access-owner");
        User applicant = saveUser("access-applicant");
        User otherUser = saveUser("access-other");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.ACTIVE);

        mockMvc.perform(post("/applications/{applicationId}/conversation", application.getId())
                .cookie(accessTokenCookie(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(messageJson("First message")))
                .andExpect(status().isCreated());

        Conversation conversation = conversationRepository.findAllForUser(owner.getId()).getFirst();

        mockMvc.perform(get("/conversations")
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].otherParticipant.id").value(owner.getId().toString()));

        mockMvc.perform(get("/conversations/{conversationId}/messages", conversation.getId())
                .cookie(accessTokenCookie(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("First message"));

        mockMvc.perform(get("/conversations/{conversationId}/messages", conversation.getId())
                .cookie(accessTokenCookie(otherUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void participantSendsAnotherMessage() {
        User owner = saveUser("message-owner");
        User applicant = saveUser("message-applicant");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.ACTIVE);
        Conversation conversation = conversationService.create(application.getId(), owner.getId(), "First");

        MessageSendResult result = conversationService.sendMessage(
                conversation.getId(),
                applicant.getId(),
                "  Reply  "
        );

        assertThat(result.senderUserId()).isEqualTo(applicant.getId());
        assertThat(result.recipientUserId()).isEqualTo(owner.getId());
        assertThat(result.message().getContent()).isEqualTo("Reply");
        assertThat(messageRepository.count()).isEqualTo(2);
        assertThat(conversationRepository.findById(conversation.getId()))
                .get()
                .satisfies(updatedConversation ->
                        assertThat(updatedConversation.getLastMessage()).isEqualTo("Reply")
                );
    }

    @Test
    void userOutsideConversationCannotSendMessage() {
        User owner = saveUser("outside-owner");
        User applicant = saveUser("outside-applicant");
        User otherUser = saveUser("outside-user");
        Application application = saveApplication(applicant, saveJob(owner), ApplicationStatus.ACTIVE);
        Conversation conversation = conversationService.create(application.getId(), owner.getId(), "First");

        assertThatThrownBy(() -> conversationService.sendMessage(
                conversation.getId(),
                otherUser.getId(),
                "Not allowed"
        )).isInstanceOf(ResourceNotFoundException.class);

        assertThat(messageRepository.count()).isOne();
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
    }

    private String messageJson(String content) {
        return """
                {
                  "content": "%s"
                }
                """.formatted(content);
    }

    private User saveUser(String emailPrefix) {
        User user = new User();
        user.setEmail(emailPrefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.ROLE_USER);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("0712345678");
        user.setTermsAcceptedAt(LocalDateTime.now());
        return userRepository.saveAndFlush(user);
    }

    private Job saveJob(User owner) {
        Job job = new Job();
        job.setTitle("Chat test job");
        job.setDescription("Conversation integration test job");
        job.setCategory(JobCategory.CLEANING);
        job.setStatus(JobStatus.OPEN);
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
