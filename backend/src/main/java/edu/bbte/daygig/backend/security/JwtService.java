package edu.bbte.daygig.backend.security;


import edu.bbte.daygig.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties
            .secret()
            .getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);
        String uuid = user.getId().toString();

        return Jwts
            .builder()
            .subject(uuid)
            .claim("uuid", uuid)
            .claim("email", user.getEmail())
            .claim("role", user.getRole() != null ? user
                .getRole()
                .name() : "ROLE_USER")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    }

    public Claims parseToken(String token) throws JwtException {
        if (token == null || token.isBlank()) {
            throw new JwtException("JWT token is missing");
        }

        Jws<Claims> parsedToken = Jwts.parser()
                                      .verifyWith(signingKey)
                                      .build()
                                      .parseSignedClaims(token);

        return parsedToken.getPayload();
    }
}
