package edu.bbte.daygig.backend.security;

import java.security.Principal;

public record JwtPrincipal(String uuid, String email, String role) implements Principal {

    @Override
    public String getName() {
        return uuid;
    }
}
