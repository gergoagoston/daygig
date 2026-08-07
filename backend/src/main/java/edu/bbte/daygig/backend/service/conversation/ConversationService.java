package edu.bbte.daygig.backend.service.conversation;

import java.util.List;
import java.util.UUID;
import edu.bbte.daygig.backend.model.Conversation;
import edu.bbte.daygig.backend.model.Message;

public interface ConversationService {

    Conversation create(UUID applicationId,
                        UUID ownerId,
                        String firstMessageContent);

    List<Conversation> getAll(UUID userId);

    List<Message> getMessages(UUID conversationId,
                              UUID userId);

    MessageSendResult sendMessage(UUID conversationId,
                                  UUID senderId,
                                  String content);
}
