package edu.bbte.daygig.backend.service.conversation;

import java.util.UUID;
import edu.bbte.daygig.backend.model.Message;

public record MessageSendResult(
        Message message,
        UUID senderUserId,
        UUID recipientUserId
) {
}
