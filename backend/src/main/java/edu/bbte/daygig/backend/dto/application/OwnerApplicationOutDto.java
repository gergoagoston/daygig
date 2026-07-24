package edu.bbte.daygig.backend.dto.application;

import java.time.LocalDateTime;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;

public record OwnerApplicationOutDto(UUID id, ApplicationStatus status, LocalDateTime appliedAt,
                                     UserSummaryOutDto applicant) {
}
