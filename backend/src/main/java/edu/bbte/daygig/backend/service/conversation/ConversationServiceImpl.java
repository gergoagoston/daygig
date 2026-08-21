package edu.bbte.daygig.backend.service.conversation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.exception.DuplicateResourceException;
import edu.bbte.daygig.backend.exception.ForbiddenException;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.exception.ServiceException;
import edu.bbte.daygig.backend.mappers.ConversationMapper;
import edu.bbte.daygig.backend.messaging.rabbit.config.RabbitNames;
import edu.bbte.daygig.backend.messaging.rabbit.event.DomainEventEnvelope;
import edu.bbte.daygig.backend.messaging.rabbit.event.JobActivityEventPayload;
import edu.bbte.daygig.backend.model.Application;
import edu.bbte.daygig.backend.model.Conversation;
import edu.bbte.daygig.backend.model.enums.ApplicationStatus;
import edu.bbte.daygig.backend.model.Message;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.ApplicationRepository;
import edu.bbte.daygig.backend.repository.ConversationRepository;
import edu.bbte.daygig.backend.repository.MessageRepository;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ApplicationRepository applicationRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Conversation create(UUID applicationId,
                               UUID currentUserId,
                               String firstMessageContent) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + applicationId
                ));

        User owner = application.getJob().getOwner();
        User applicant = application.getApplicant();
        User sender;
        User recipient;
        if (owner.getId().equals(currentUserId)) {
            sender = owner;
            recipient = applicant;
        } else if (applicant.getId().equals(currentUserId)) {
            sender = applicant;
            recipient = owner;
        } else {
            throw new ForbiddenException("Only job participants can start a conversation");
        }

        ensureActive(application);
        ensureConversationDoesNotExist(applicationId);

        Instant now = Instant.now();
        String content = firstMessageContent.trim();

        Conversation conversation = new Conversation();
        conversation.setApplication(application);
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(now);
        Conversation savedConversation = conversationRepository.saveAndFlush(conversation);

        Message firstMessage = new Message();
        firstMessage.setConversation(savedConversation);
        firstMessage.setSender(sender);
        firstMessage.setContent(content);
        messageRepository.saveAndFlush(firstMessage);

        eventPublisher.publishEvent(DomainEventEnvelope.create(
                RabbitNames.CONVERSATION_STARTED,
                recipient.getId(),
                JobActivityEventPayload.forChat(
                        application.getJob().getId(),
                        savedConversation.getId(),
                        sender.getId()
                )
        ));

        return savedConversation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getAll(UUID userId) {
        return conversationRepository.findAllForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId,
                                     UUID userId) {
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with id: " + conversationId
                ));
        ensureParticipant(conversation, userId);

        return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    @Transactional
    public MessageSendResult sendMessage(UUID conversationId,
                                         UUID senderId,
                                         String messageContent) {
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with id: " + conversationId
                ));

        Application application = conversation.getApplication();
        User owner = application.getJob().getOwner();
        User applicant = application.getApplicant();

        User sender;
        User recipient;
        if (owner.getId().equals(senderId)) {
            sender = owner;
            recipient = applicant;
        } else if (applicant.getId().equals(senderId)) {
            sender = applicant;
            recipient = owner;
        } else {
            throw new ResourceNotFoundException(
                    "Conversation not found with id: " + conversationId
            );
        }

        String content = normalizeMessageContent(messageContent);
        Instant now = Instant.now();

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);

        conversation.setLastMessage(content);
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);

        Message savedMessage = messageRepository.saveAndFlush(message);
        eventPublisher.publishEvent(DomainEventEnvelope.create(
                RabbitNames.MESSAGE_CREATED,
                recipient.getId(),
                JobActivityEventPayload.forChat(
                        application.getJob().getId(),
                        conversation.getId(),
                        sender.getId()
                )
        ));
        return new MessageSendResult(
                conversationMapper.toDto(savedMessage),
                sender.getId(),
                recipient.getId()
        );
    }

    private void ensureActive(Application application) {
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ServiceException("A conversation can only be started for an active application");
        }
    }

    private void ensureParticipant(Conversation conversation,
                                   UUID userId) {
        Application application = conversation.getApplication();
        boolean isApplicant = application.getApplicant().getId().equals(userId);
        boolean isOwner = application.getJob().getOwner().getId().equals(userId);

        if (!isApplicant && !isOwner) {
            throw new ResourceNotFoundException(
                    "Conversation not found with id: " + conversation.getId()
            );
        }
    }

    private void ensureConversationDoesNotExist(UUID applicationId) {
        if (conversationRepository.existsByApplicationId(applicationId)) {
            throw new DuplicateResourceException("A conversation already exists for this application");
        }
    }

    private String normalizeMessageContent(String messageContent) {
        if (messageContent == null || messageContent.isBlank()) {
            throw new ServiceException("Message content must not be blank");
        }

        String content = messageContent.trim();
        if (content.length() > 200) {
            throw new ServiceException("Message content must not exceed 200 characters");
        }
        return content;
    }
}
