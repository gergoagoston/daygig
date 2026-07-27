package edu.bbte.daygig.backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileOutDto(UUID id, String firstName, String lastName, String bio,
                                LocalDateTime createdAt) {
}
