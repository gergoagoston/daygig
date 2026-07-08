package edu.bbte.daygig.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   AuthCookieService authCookieService) {
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = authCookieService.extractAccessToken(request).orElse(null);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtService.parseToken(token);

            String uuid = claims.get("uuid", String.class);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);


            if (uuid == null || uuid.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
            if (email == null || email.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
            if (role == null || role.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }

            UUID.fromString(uuid);

            if (SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {
                JwtPrincipal principal = new JwtPrincipal(uuid, email, role);

                List<SimpleGrantedAuthority> authorities = Collections.emptyList();


                String formattedRole = role.startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();

                authorities = List.of(new SimpleGrantedAuthority(formattedRole));


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
                    null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
        }
    }
}
