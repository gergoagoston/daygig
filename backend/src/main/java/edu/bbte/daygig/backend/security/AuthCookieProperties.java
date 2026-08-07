package edu.bbte.daygig.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth-cookie")
public record AuthCookieProperties(String name, boolean secure, String sameSite, String path, String domain) {
}
