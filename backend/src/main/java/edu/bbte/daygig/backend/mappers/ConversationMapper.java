package edu.bbte.daygig.backend.mappers;

import java.util.UUID;
import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.conversation.ConversationOutDto;
import edu.bbte.daygig.backend.dto.conversation.MessageOutDto;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.Conversation;
import edu.bbte.daygig.backend.model.Message;
import edu.bbte.daygig.backend.model.User;

@Component
public class ConversationMapper {

    public ConversationOutDto toDto(Conversation conversation,
                                    UUID currentUserId) {
        Application application = conversation.getApplication();
        User owner = application.getJob().getOwner();
        User otherParticipant = owner.getId().equals(currentUserId) ? application.getApplicant() : owner;

        return new ConversationOutDto(
                conversation.getId(),
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                toUserSummary(otherParticipant),
                conversation.getLastMessage(),
                conversation.getLastMessageAt()
        );
    }

    public MessageOutDto toDto(Message message) {
        return new MessageOutDto(
                message.getId(),
                message.getConversation().getId(),
                toUserSummary(message.getSender()),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private UserSummaryOutDto toUserSummary(User user) {
        return new UserSummaryOutDto(user.getId(), user.getFirstName(), user.getLastName());
    }
}
