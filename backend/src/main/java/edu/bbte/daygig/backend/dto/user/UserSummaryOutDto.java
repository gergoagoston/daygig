package edu.bbte.daygig.backend.dto.user;

import java.util.UUID;

public record UserSummaryOutDto(UUID id, String firstName, String lastName) {
}
