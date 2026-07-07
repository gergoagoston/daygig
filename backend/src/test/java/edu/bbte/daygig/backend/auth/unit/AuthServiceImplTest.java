package edu.bbte.daygig.backend.auth.unit;

import edu.bbte.daygig.backend.exception.AuthException;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
import edu.bbte.daygig.backend.model.Role;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.UserRepository;
import edu.bbte.daygig.backend.service.auth.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD1 = "password123";
    private static final String PASSWORD2 = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginReturnsUserWhenCredentialsAreValid() {
        User loginUser = user(EMAIL, PASSWORD1);
        User storedUser = user(EMAIL, PASSWORD2);

        when(userRepository.findByEmail(EMAIL)).thenReturn(storedUser);
        when(passwordEncoder.matches(PASSWORD1, PASSWORD2)).thenReturn(true);

        User result = authService.login(loginUser);

        assertThat(result).isSameAs(storedUser);
    }

    @Test
    void loginRejectsUnknownEmail() {
        User loginUser = user(EMAIL, PASSWORD1);

        when(userRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThatThrownBy(() -> authService.login(loginUser))
            .isInstanceOf(AuthException.class)
            .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginRejectsInvalidPassword() {
        User loginUser = user(EMAIL, PASSWORD1);
        User storedUser = user(EMAIL, PASSWORD2);

        when(userRepository.findByEmail(EMAIL)).thenReturn(storedUser);
        when(passwordEncoder.matches(PASSWORD1, PASSWORD2)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginUser))
            .isInstanceOf(AuthException.class)
            .hasMessage("Invalid email or password");
    }

    @Test
    void registerEncodesPasswordAssignsUserRoleAndSavesUser() {
        User newUser = user(EMAIL, PASSWORD1);

        when(userRepository.findByEmail(EMAIL)).thenReturn(null);
        when(passwordEncoder.encode(PASSWORD1)).thenReturn(PASSWORD2);
        when(userRepository.save(newUser)).thenReturn(newUser);

        User result = authService.register(newUser);

        assertThat(result).isSameAs(newUser);
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getPassword()).isEqualTo(PASSWORD2);
        verify(userRepository).save(newUser);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        User newUser = user(EMAIL, PASSWORD1);
        User existingUser = user(EMAIL, PASSWORD2);

        when(userRepository.findByEmail(EMAIL)).thenReturn(existingUser);

        assertThatThrownBy(() -> authService.register(newUser))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Email is already taken");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private User user(String email,
                      String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("0712345678");
        return user;
    }
}
