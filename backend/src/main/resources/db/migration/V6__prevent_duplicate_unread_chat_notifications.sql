DROP INDEX idx_notifications_recipient_conversation_read;

DELETE FROM notifications
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY recipient_id, conversation_id
                   ORDER BY created_at DESC, id DESC
               ) AS row_number
        FROM notifications
        WHERE conversation_id IS NOT NULL
          AND is_read = FALSE
    ) ranked_notifications
    WHERE row_number > 1
);

CREATE UNIQUE INDEX uk_notifications_unread_conversation
    ON notifications (recipient_id, conversation_id)
    WHERE conversation_id IS NOT NULL
      AND is_read = FALSE;
