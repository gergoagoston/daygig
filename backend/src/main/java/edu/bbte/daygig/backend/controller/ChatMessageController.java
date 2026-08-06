package edu.bbte.daygig.backend.controller;

import java.security.Principal;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.conversation.MessageCreateInDto;
import edu.bbte.daygig.backend.dto.conversation.MessageOutDto;
import edu.bbte.daygig.backend.messaging.websocket.publisher.ChatMessageWebSocketPublisher;
import edu.bbte.daygig.backend.service.conversation.ConversationService;
import edu.bbte.daygig.backend.service.conversation.MessageSendResult;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ConversationService conversationService;
    private final ChatMessageWebSocketPublisher webSocketPublisher;

    @MessageMapping("/conversations/{conversationId}/messages")
    public void send(@DestinationVariable UUID conversationId,
                     @Valid @Payload MessageCreateInDto input,
                     Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());
        MessageSendResult result = conversationService.sendMessage(
                conversationId,
                senderId,
                input.content()
        );
        MessageOutDto message = result.message();

        webSocketPublisher.publish(result.senderUserId(), message);
        webSocketPublisher.publish(result.recipientUserId(), message);
    }
}
