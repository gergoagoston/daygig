CREATE TABLE notifications (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    event_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    job_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT uk_notifications_event_id UNIQUE (event_id),
    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id)
        REFERENCES user_accounts (id)
);

CREATE INDEX idx_notifications_recipient_read_created
    ON notifications (recipient_id, is_read, created_at DESC);
