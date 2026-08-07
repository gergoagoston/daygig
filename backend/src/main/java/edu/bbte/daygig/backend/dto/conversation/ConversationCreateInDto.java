package edu.bbte.daygig.backend.dto.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConversationCreateInDto(
        @NotBlank @Size(max = 200) String content
) {
}
