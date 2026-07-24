package edu.bbte.daygig.backend.dto.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;

@Data
public class ApplicationStatusUpdateInDto {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
}
