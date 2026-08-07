ALTER TABLE notifications
    ADD COLUMN conversation_id UUID;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations (id);

CREATE INDEX idx_notifications_recipient_conversation_read
    ON notifications (recipient_id, conversation_id, is_read);
