package edu.bbte.daygig.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void registerCreatesUserAndReturnsToken() throws Exception {
        TestUser user = testUser("register-success", "Register", "Success");

        register(user)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token", not(blankOrNullString())))
            .andExpect(jsonPath("$.user.email").value(user.email()))
            .andExpect(jsonPath("$.user.role").value("ROLE_USER"))
            .andExpect(jsonPath("$.user.firstName").value(user.firstName()))
            .andExpect(jsonPath("$.user.lastName").value(user.lastName()))
            .andExpect(jsonPath("$.user.phoneNumber").value(user.phoneNumber()))
            .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        TestUser user = testUser("duplicate", "Duplicate", "User");

        register(user).andExpect(status().isCreated());

        register(user)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Business Logic Error"))
            .andExpect(jsonPath("$.message").value("Email is already taken"));
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        TestUser user = testUser("login-success", "Login", "Success");

        register(user).andExpect(status().isCreated());

        login(user.email(), user.password())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", not(blankOrNullString())))
            .andExpect(jsonPath("$.user.email").value(user.email()))
            .andExpect(jsonPath("$.user.role").value("ROLE_USER"))
            .andExpect(jsonPath("$.user.password").doesNotExist());
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
            .andExpect(jsonPath("$.details.lastName").value("Last name is required"))
            .andExpect(jsonPath("$.details.phoneNumber").value("Phone number is required"));
    }

    private TestUser testUser(String emailPrefix,
                              String firstName,
                              String lastName) {
        return new TestUser(emailPrefix + "@example.com", PASSWORD, firstName, lastName, PHONE_NUMBER);
    }

    private org.springframework.test.web.servlet.ResultActions register(TestUser user) throws Exception {
        return mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerRequest(user)));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email,
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
              "phoneNumber": "%s"
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
