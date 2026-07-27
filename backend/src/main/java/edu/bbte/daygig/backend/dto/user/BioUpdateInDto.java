package edu.bbte.daygig.backend.dto.user;

import jakarta.validation.constraints.Size;

public record BioUpdateInDto(
        @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
        String bio) {
}
