package edu.bbte.daygig.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.bbte.daygig.backend.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    boolean existsByApplicationId(UUID applicationId);

    @Query("""
            SELECT conversation
            FROM Conversation conversation
            JOIN FETCH conversation.application application
            JOIN FETCH application.applicant applicant
            JOIN FETCH application.job job
            JOIN FETCH job.owner owner
            WHERE applicant.id = :userId OR owner.id = :userId
            ORDER BY conversation.lastMessageAt DESC
            """)
    List<Conversation> findAllForUser(@Param("userId") UUID userId);

}
