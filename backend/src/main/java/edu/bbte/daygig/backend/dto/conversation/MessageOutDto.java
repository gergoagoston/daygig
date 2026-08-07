package edu.bbte.daygig.backend.dto.conversation;

import java.time.Instant;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;

public record MessageOutDto(
        UUID id,
        UUID conversationId,
        UserSummaryOutDto sender,
        String content,
        Instant createdAt
) {
}
