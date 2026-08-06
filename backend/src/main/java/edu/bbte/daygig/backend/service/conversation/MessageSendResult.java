package edu.bbte.daygig.backend.service.conversation;

import java.util.UUID;
import edu.bbte.daygig.backend.dto.conversation.MessageOutDto;

public record MessageSendResult(
        MessageOutDto message,
        UUID senderUserId,
        UUID recipientUserId
) {
}
