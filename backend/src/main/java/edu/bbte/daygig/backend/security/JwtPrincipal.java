package edu.bbte.daygig.backend.security;

public record JwtPrincipal(String uuid, String email, String role) {
}
