package edu.bbte.daygig.backend.messaging.websocket.publisher;

import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.conversation.MessageOutDto;

@Component
@RequiredArgsConstructor
public class ChatMessageWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(UUID userId,
                        MessageOutDto message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/exchange/amq.direct/messages",
                message
        );
    }
}
