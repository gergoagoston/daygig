package edu.bbte.daygig.backend.dto.rating;

import java.util.UUID;

public record RatingParticipantOutDto(UUID id,
                                      String firstName,
                                      String lastName,
                                      String avatarUrl) {
}
