package edu.bbte.daygig.backend.auth.unit;

import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import edu.bbte.daygig.backend.security.AuthCookieService;
import edu.bbte.daygig.backend.security.JwtAuthenticationFilter;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "jwt-token";
    private static final String EMAIL = "filter-user@example.com";

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthCookieService authCookieService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterContinuesWithoutAuthenticationWhenCookieIsMissing() throws Exception {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authCookieService.extractAccessToken(request)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).parseToken(TOKEN);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterAuthenticatesRequestWhenTokenIsValid() throws Exception {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String uuid = UUID.randomUUID().toString();
        when(authCookieService.extractAccessToken(request)).thenReturn(Optional.of(TOKEN));
        when(jwtService.parseToken(TOKEN)).thenReturn(claims);
        when(claims.get("uuid", String.class)).thenReturn(uuid);
        when(claims.get("email", String.class)).thenReturn(EMAIL);
        when(claims.get("role", String.class)).thenReturn("USER");

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new JwtPrincipal(uuid, EMAIL, "USER"));
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterContinuesWithoutAuthenticationAndClearsCookieWhenTokenIsInvalid() throws Exception {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authCookieService.extractAccessToken(request)).thenReturn(Optional.of(TOKEN));
        when(jwtService.parseToken(TOKEN)).thenThrow(new JwtException("bad token"));
        when(authCookieService.clearAccessTokenCookie()).thenReturn("accessToken=; Max-Age=0; Path=/api");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterContinuesWithoutAuthenticationWhenRequiredClaimIsMissing() throws Exception {
        JwtAuthenticationFilter filter = filter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authCookieService.extractAccessToken(request)).thenReturn(Optional.of(TOKEN));
        when(jwtService.parseToken(TOKEN)).thenReturn(claims);
        when(claims.get("uuid", String.class)).thenReturn(UUID.randomUUID().toString());
        when(claims.get("email", String.class)).thenReturn("");
        when(authCookieService.clearAccessTokenCookie()).thenReturn("accessToken=; Max-Age=0; Path=/api");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private JwtAuthenticationFilter filter() {
        return new JwtAuthenticationFilter(jwtService, authCookieService);
    }
}
