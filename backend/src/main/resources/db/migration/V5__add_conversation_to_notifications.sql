ALTER TABLE notifications
    ADD COLUMN conversation_id UUID;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations (id);

CREATE UNIQUE INDEX uk_notifications_unread_conversation
    ON notifications (recipient_id, conversation_id)
    WHERE conversation_id IS NOT NULL
      AND is_read = FALSE;
