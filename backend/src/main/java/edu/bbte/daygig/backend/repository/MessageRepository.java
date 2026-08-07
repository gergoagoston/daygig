package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.bbte.daygig.backend.model.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @EntityGraph(attributePaths = "sender")
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
