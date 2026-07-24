package edu.bbte.daygig.backend.auth.unit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import edu.bbte.daygig.backend.security.AuthCookieProperties;
import edu.bbte.daygig.backend.security.AuthCookieService;
import edu.bbte.daygig.backend.security.JwtProperties;
import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieServiceTest {

    private static final String COOKIE_NAME = "accessToken";
    private static final String TOKEN = "jwt-token";

    private final AuthCookieService authCookieService = new AuthCookieService(
            new AuthCookieProperties(COOKIE_NAME, false, "Strict", "/api"),
            new JwtProperties("test-secret-key-that-is-long-enough-for-hmac-sha256", 60L));

    @Test
    void extractAccessTokenReturnsEmptyWhenRequestHasNoCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(authCookieService.extractAccessToken(request)).isEmpty();
    }

    @Test
    void createAccessTokenCookieUsesConfiguredSecurityAttributes() {
        String cookie = authCookieService.createAccessTokenCookie(TOKEN);

        assertThat(cookie)
                .contains(COOKIE_NAME + "=" + TOKEN)
                .contains("Path=/api")
                .contains("Max-Age=3600")
                .contains("HttpOnly")
                .contains("SameSite=Strict")
                .doesNotContain("Secure");
    }

    @Test
    void clearAccessTokenCookieExpiresCookieImmediately() {
        String cookie = authCookieService.clearAccessTokenCookie();

        assertThat(cookie)
                .contains(COOKIE_NAME + "=")
                .contains("Path=/api")
                .contains("Max-Age=0")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }
}
