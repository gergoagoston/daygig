package edu.bbte.daygig.backend.auth.unit;

import edu.bbte.daygig.backend.model.Role;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.security.JwtProperties;
import edu.bbte.daygig.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String JWT_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256";
    private static final long EXPIRATION_MINUTES = 60L;

    @Test
    void generateTokenContainsUserIdentityAndRoleClaims() {
        JwtService jwtService = jwtService(EXPIRATION_MINUTES);
        User user = user(Role.ROLE_ADMIN);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);

        assertThat(token).isNotBlank();
        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.get("uuid", String.class)).isEqualTo(user.getId().toString());
        assertThat(claims.get("email", String.class)).isEqualTo(user.getEmail());
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void generateTokenUsesUserRoleFallbackWhenRoleIsMissing() {
        JwtService jwtService = jwtService(EXPIRATION_MINUTES);
        User user = user(null);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseToken(token);

        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_USER");
    }

    @Test
    void parseTokenRejectsTamperedToken() {
        JwtService jwtService = jwtService(EXPIRATION_MINUTES);
        String token = jwtService.generateToken(user(Role.ROLE_USER));
        String tamperedToken = token.substring(0, token.length() - 1) + "x";

        assertThatThrownBy(() -> jwtService.parseToken(tamperedToken)).isInstanceOf(JwtException.class);
    }

    @Test
    void parseTokenRejectsBlankToken() {
        JwtService jwtService = jwtService(EXPIRATION_MINUTES);

        assertThatThrownBy(() -> jwtService.parseToken(" ")).isInstanceOf(JwtException.class)
            .hasMessage("JWT token is missing");
    }

    @Test
    void parseTokenRejectsExpiredToken() {
        JwtService jwtService = jwtService(-1L);
        String token = jwtService.generateToken(user(Role.ROLE_USER));

        assertThatThrownBy(() -> jwtService.parseToken(token)).isInstanceOf(JwtException.class);
    }

    private JwtService jwtService(long expirationMinutes) {
        return new JwtService(new JwtProperties(JWT_SECRET, expirationMinutes));
    }

    private User user(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jwt-user@example.com");
        user.setRole(role);
        return user;
    }
}
