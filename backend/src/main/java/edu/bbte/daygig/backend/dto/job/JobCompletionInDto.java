package edu.bbte.daygig.backend.dto.job;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record JobCompletionInDto(@NotNull UUID applicationId) {
}
