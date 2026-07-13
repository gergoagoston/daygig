package edu.bbte.daygig.backend.dto.user;

import edu.bbte.daygig.backend.model.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserOutDto(UUID id,
                         String email,
                         Role role,
                         String firstName,
                         String lastName,
                         String phoneNumber,
                         String bio,
                         LocalDateTime createdAt) {
}
