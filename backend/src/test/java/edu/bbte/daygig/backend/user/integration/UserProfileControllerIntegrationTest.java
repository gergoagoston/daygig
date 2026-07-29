package edu.bbte.daygig.backend.user.integration;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.security.JwtService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerIntegrationTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void updateBioChangesOnlyAuthenticatedUserBio() throws Exception {
        User currentUser = saveUser("current-user");
        User otherUser = saveUser("other-user");
        otherUser.setBio("Other user bio");
        userRepository.saveAndFlush(otherUser);

        mockMvc.perform(patch("/users/me")
                .cookie(accessTokenCookie(currentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "bio": "  Updated profile bio  "
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.email").value(currentUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(currentUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(currentUser.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(currentUser.getPhoneNumber()))
                .andExpect(jsonPath("$.bio").value("Updated profile bio"));

        User updatedCurrentUser = userRepository.findById(currentUser.getId()).orElseThrow();
        User unchangedOtherUser = userRepository.findById(otherUser.getId()).orElseThrow();
        assertThat(updatedCurrentUser.getBio()).isEqualTo("Updated profile bio");
        assertThat(updatedCurrentUser.getEmail()).isEqualTo(currentUser.getEmail());
        assertThat(updatedCurrentUser.getFirstName()).isEqualTo(currentUser.getFirstName());
        assertThat(updatedCurrentUser.getLastName()).isEqualTo(currentUser.getLastName());
        assertThat(updatedCurrentUser.getPhoneNumber()).isEqualTo(currentUser.getPhoneNumber());
        assertThat(unchangedOtherUser.getBio()).isEqualTo("Other user bio");
    }

    @Test
    void updateBioStoresBlankValueAsNull() throws Exception {
        User user = saveUser("clear-bio");
        user.setBio("Existing bio");
        userRepository.saveAndFlush(user);

        mockMvc.perform(patch("/users/me")
                .cookie(accessTokenCookie(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "bio": "   "
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value(nullValue()));

        assertThat(userRepository.findById(user.getId()).orElseThrow().getBio()).isNull();
    }

    @Test
    void updateBioRejectsValueLongerThanOneThousandCharacters() throws Exception {
        User user = saveUser("long-bio");

        mockMvc.perform(patch("/users/me")
                .cookie(accessTokenCookie(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "bio": "%s"
                        }
                        """.formatted("a".repeat(1001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.bio").value("Bio cannot exceed 1000 characters"));
    }

    @Test
    void updateBioRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(patch("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "bio": "Unauthorized update"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfileReturnsOnlyPublicProfileFieldsForAuthenticatedUser() throws Exception {
        User viewer = saveUser("profile-viewer");
        User profileOwner = saveUser("profile-owner");
        profileOwner.setBio("Public profile bio");
        userRepository.saveAndFlush(profileOwner);

        mockMvc.perform(get("/users/{id}/profile", profileOwner.getId())
                .cookie(accessTokenCookie(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileOwner.getId().toString()))
                .andExpect(jsonPath("$.firstName").value(profileOwner.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(profileOwner.getLastName()))
                .andExpect(jsonPath("$.bio").value(profileOwner.getBio()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.termsAcceptedAt").doesNotExist());
    }

    @Test
    void getProfileRejectsUnauthenticatedRequest() throws Exception {
        User profileOwner = saveUser("private-profile");

        mockMvc.perform(get("/users/{id}/profile", profileOwner.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfileReturnsNotFoundForUnknownUser() throws Exception {
        User viewer = saveUser("missing-profile-viewer");

        mockMvc.perform(get("/users/{id}/profile", UUID.randomUUID())
                .cookie(accessTokenCookie(viewer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    void legacyUserManagementEndpointsAreNotAvailable() throws Exception {
        User user = saveUser("legacy-endpoints");
        Cookie accessTokenCookie = accessTokenCookie(user);

        mockMvc.perform(get("/users").cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/users/{id}", user.getId()).cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/users/{id}", user.getId())
                .cookie(accessTokenCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/users/{id}", user.getId()).cookie(accessTokenCookie))
                .andExpect(status().isNotFound());
    }

    private Cookie accessTokenCookie(User user) {
        return new Cookie(ACCESS_TOKEN_COOKIE_NAME, jwtService.generateToken(user));
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
}
