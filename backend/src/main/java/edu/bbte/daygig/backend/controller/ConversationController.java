package edu.bbte.daygig.backend.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.conversation.ConversationCreateInDto;
import edu.bbte.daygig.backend.dto.conversation.ConversationOutDto;
import edu.bbte.daygig.backend.dto.conversation.MessageOutDto;
import edu.bbte.daygig.backend.mappers.ConversationMapper;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.conversation.ConversationService;

@RestController
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper;

    @PostMapping("/applications/{applicationId}/conversation")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationOutDto create(@PathVariable UUID applicationId,
                                     @Valid @RequestBody ConversationCreateInDto createInDto,
                                     @AuthenticationPrincipal JwtPrincipal principal) {
        UUID currentUserId = currentUserId(principal);
        return conversationMapper.toDto(
                conversationService.create(applicationId, currentUserId, createInDto.content()),
                currentUserId
        );
    }

    @GetMapping("/conversations")
    public List<ConversationOutDto> getAll(@AuthenticationPrincipal JwtPrincipal principal) {
        UUID currentUserId = currentUserId(principal);
        return conversationService
                .getAll(currentUserId)
                .stream()
                .map(conversation -> conversationMapper.toDto(conversation, currentUserId))
                .toList();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<MessageOutDto> getMessages(@PathVariable UUID conversationId,
                                           @AuthenticationPrincipal JwtPrincipal principal) {
        return conversationService
                .getMessages(conversationId, currentUserId(principal))
                .stream()
                .map(conversationMapper::toDto)
                .toList();
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
