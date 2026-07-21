package edu.bbte.daygig.backend.security;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            Map<String, String> body = Map.of("error", "Unauthorized: Missing or invalid token");
                            response
                                    .getWriter()
                                    .write(new ObjectMapper().writeValueAsString(body));
                        }))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/mine")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/jobs")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs/{id}")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
