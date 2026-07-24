package edu.bbte.daygig.backend.auth.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "password123";
    private static final String PHONE_NUMBER = "0712345678";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerCreatesUserWithoutAuthenticating() throws Exception {
        TestUser user = testUser("register-success", "Register", "Success");

        register(user)
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andExpect(content().string(""));

        User savedUser = userRepository.findByEmail(user.email());
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getTermsAcceptedAt()).isNotNull();
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        TestUser user = testUser("duplicate", "Duplicate", "User");

        register(user).andExpect(status().isCreated());

        register(user)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Resource Conflict"))
                .andExpect(jsonPath("$.message").value("Email is already taken"));
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        TestUser user = testUser("login-success", "Login", "Success");

        register(user).andExpect(status().isCreated());

        login(user.email(), user.password())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("accessToken=")))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.firstName").value(user.firstName()))
                .andExpect(jsonPath("$.lastName").value(user.lastName()))
                .andExpect(jsonPath("$.phoneNumber").value(user.phoneNumber()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginRejectsInvalidPassword() throws Exception {
        TestUser user = testUser("bad-password", "Bad", "Password");

        register(user).andExpect(status().isCreated());

        login(user.email(), "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Failed"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void logoutClearsAccessTokenCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("accessToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    void meReturnsCurrentUserWhenTokenCookieIsValid() throws Exception {
        TestUser user = testUser("me-success", "Current", "User");

        register(user).andExpect(status().isCreated());
        Cookie accessTokenCookie = login(user.email(), user.password())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("accessToken");

        mockMvc.perform(get("/auth/me")
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.firstName").value(user.firstName()))
                .andExpect(jsonPath("$.lastName").value(user.lastName()))
                .andExpect(jsonPath("$.phoneNumber").value(user.phoneNumber()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void meRejectsRequestWithoutTokenCookie() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized: Missing or invalid token"));
    }

    @Test
    void registerRejectsInvalidInput() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "not-an-email",
                      "password": "short",
                      "firstName": "",
                      "lastName": "",
                      "phoneNumber": ""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.email").value("Invalid email format"))
                .andExpect(jsonPath("$.details.password").value("Password must be at least 8 characters long"))
                .andExpect(jsonPath("$.details.firstName").value("First name is required"))
                .andExpect(jsonPath("$.details.lastName").value("Last name is required"));
    }

    @Test
    void registerRejectsMissingTermsAcceptance() throws Exception {
        TestUser user = testUser("missing-terms", "Missing", "Terms");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "firstName": "%s",
                      "lastName": "%s",
                      "phoneNumber": "%s"
                    }
                    """.formatted(user.email(), user.password(), user.firstName(), user.lastName(),
                        user.phoneNumber())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.termsAccepted")
                        .value("Terms and Conditions consent is required"));
    }

    @Test
    void registerRejectsDeclinedTermsAcceptance() throws Exception {
        TestUser user = testUser("declined-terms", "Declined", "Terms");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "firstName": "%s",
                      "lastName": "%s",
                      "phoneNumber": "%s",
                      "termsAccepted": false
                    }
                    """.formatted(user.email(), user.password(), user.firstName(), user.lastName(),
                        user.phoneNumber())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.termsAccepted")
                        .value("Terms and Conditions must be accepted"));
    }

    private TestUser testUser(String emailPrefix,
                              String firstName,
                              String lastName) {
        return new TestUser(emailPrefix + "@example.com", PASSWORD, firstName, lastName, PHONE_NUMBER);
    }

    private ResultActions register(TestUser user) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest(user)));
    }

    private ResultActions login(String email,
                                String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest(email, password)));
    }

    private String registerRequest(TestUser user) {
        return """
            {
              "email": "%s",
              "password": "%s",
              "firstName": "%s",
              "lastName": "%s",
              "phoneNumber": "%s",
              "termsAccepted": true
            }
            """.formatted(user.email(), user.password(), user.firstName(), user.lastName(), user.phoneNumber());
    }

    private String loginRequest(String email,
                                String password) {
        return """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);
    }

    private record TestUser(String email,
                            String password,
                            String firstName,
                            String lastName,
                            String phoneNumber) {
    }
}
