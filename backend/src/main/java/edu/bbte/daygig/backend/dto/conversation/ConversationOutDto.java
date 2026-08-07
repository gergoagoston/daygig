package edu.bbte.daygig.backend.dto.conversation;

import java.time.Instant;
import java.util.UUID;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;

public record ConversationOutDto(
        UUID id,
        UUID applicationId,
        UUID jobId,
        String jobTitle,
        UserSummaryOutDto otherParticipant,
        String lastMessage,
        Instant lastMessageAt
) {
}
