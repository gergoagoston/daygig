package edu.bbte.daygig.backend.dto.application;

import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;

public record ApplicationOutDto(UUID id, ApplicationStatus status, LocalDateTime appliedAt,
                                ApplicationJobOutDto job) {
}
