package edu.bbte.daygig.backend.user.integration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.imageio.ImageIO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.UserAvatarRepository;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.security.JwtService;
import edu.bbte.daygig.backend.service.avatar.AvatarImageProcessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserAvatarControllerIntegrationTest {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAvatarRepository userAvatarRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void uploadPngStoresReadableAvatarWithPreservedAspectRatio() throws Exception {
        User user = saveUser("png-avatar");
        MockMultipartFile avatar = imageFile("avatar.png", "image/png", 600, 300, "PNG");

        mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/avatar")
                .file(avatar)
                .cookie(accessTokenCookie(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("/api/users/" + user.getId() + "/avatar"));

        MvcResult avatarResult = mockMvc.perform(get("/users/{id}/avatar", user.getId())
                .cookie(accessTokenCookie(user)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache"))
                .andReturn();

        BufferedImage storedImage = ImageIO.read(new ByteArrayInputStream(avatarResult.getResponse()
                .getContentAsByteArray()));
        assertThat(storedImage).isNotNull();
        assertThat(storedImage.getWidth()).isEqualTo(256);
        assertThat(storedImage.getHeight()).isEqualTo(128);
    }

    @Test
    void uploadReplacesExistingAvatarAndPreservesDetectedFormat() throws Exception {
        User user = saveUser("replace-avatar");
        MockMultipartFile pngAvatar = imageFile("first.png", "image/png", 100, 100, "PNG");
        MockMultipartFile jpegAvatar = imageFile("second.jpg", "image/jpeg", 100, 200, "JPEG");

        uploadAvatar(user, pngAvatar).andExpect(status().isOk());
        uploadAvatar(user, jpegAvatar).andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/avatar", user.getId())
                .cookie(accessTokenCookie(user)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE));

        assertThat(userAvatarRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteAvatarIsIdempotentAndRestoresMissingAvatarState() throws Exception {
        User user = saveUser("delete-avatar");
        uploadAvatar(user, imageFile("avatar.png", "image/png", 100, 100, "PNG"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/me/avatar").cookie(accessTokenCookie(user)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/users/me/avatar").cookie(accessTokenCookie(user)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/me").cookie(accessTokenCookie(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
        mockMvc.perform(get("/users/{id}/avatar", user.getId()).cookie(accessTokenCookie(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadRejectsFileThatCannotBeDecodedAsImage() throws Exception {
        User user = saveUser("invalid-avatar");
        MockMultipartFile invalidImage = new MockMultipartFile("file", "avatar.png", "image/png",
                "not-an-image".getBytes());

        uploadAvatar(user, invalidImage)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded file is not a valid JPEG or PNG image"));
    }

    @Test
    void uploadRejectsUnsupportedImageFormat() throws Exception {
        User user = saveUser("gif-avatar");
        MockMultipartFile gifImage = imageFile("avatar.gif", "image/gif", 100, 100, "GIF");

        uploadAvatar(user, gifImage)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Avatar image must be a JPEG or PNG file"));
    }

    @Test
    void uploadRejectsFileLargerThanTwoMegabytes() throws Exception {
        User user = saveUser("large-avatar");
        byte[] oversizedContent = new byte[(int) AvatarImageProcessor.MAX_FILE_SIZE_BYTES + 1];
        MockMultipartFile oversizedImage = new MockMultipartFile("file", "large.png", "image/png",
                oversizedContent);

        uploadAvatar(user, oversizedImage)
                .andExpect(status().is4xxClientError());
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        User user = saveUser("empty-avatar");
        MockMultipartFile emptyImage = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        uploadAvatar(user, emptyImage)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Avatar image is required"));
    }

    @Test
    void avatarEndpointsRequireAuthentication() throws Exception {
        User user = saveUser("protected-avatar");
        MockMultipartFile avatar = imageFile("avatar.png", "image/png", 100, 100, "PNG");

        mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/avatar").file(avatar))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/users/me/avatar"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users/{id}/avatar", user.getId()))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions uploadAvatar(User user,
                                                                            MockMultipartFile avatar)
            throws Exception {
        return mockMvc.perform(multipart(HttpMethod.PUT, "/users/me/avatar")
                .file(avatar)
                .cookie(accessTokenCookie(user)));
    }

    private MockMultipartFile imageFile(String fileName,
                                        String contentType,
                                        int width,
                                        int height,
                                        String format) throws Exception {
        int imageType = "PNG".equals(format) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(image, format, output)).isTrue();
        return new MockMultipartFile("file", fileName, contentType, output.toByteArray());
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
