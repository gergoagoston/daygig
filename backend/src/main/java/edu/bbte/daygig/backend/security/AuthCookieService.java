package edu.bbte.daygig.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieService {

    private final AuthCookieProperties authCookieProperties;
    private final JwtProperties jwtProperties;

    public AuthCookieService(AuthCookieProperties authCookieProperties,
                             JwtProperties jwtProperties) {
        this.authCookieProperties = authCookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays
            .stream(cookies)
            .filter(cookie -> authCookieProperties.name().equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    public String createAccessTokenCookie(String token) {
        return ResponseCookie
            .from(authCookieProperties.name(), token)
            .httpOnly(true)
            .secure(authCookieProperties.secure())
            .sameSite(authCookieProperties.sameSite())
            .path(authCookieProperties.path())
            .maxAge(Duration.ofMinutes(jwtProperties.expirationMinutes()))
            .build()
            .toString();
    }

    public String clearAccessTokenCookie() {
        return ResponseCookie
            .from(authCookieProperties.name(), "")
            .httpOnly(true)
            .secure(authCookieProperties.secure())
            .sameSite(authCookieProperties.sameSite())
            .path(authCookieProperties.path())
            .maxAge(Duration.ZERO)
            .build()
            .toString();
    }
}
