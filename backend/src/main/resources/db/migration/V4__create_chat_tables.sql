CREATE TABLE conversations (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    application_id UUID NOT NULL,
    last_message VARCHAR(200) NOT NULL,
    last_message_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_conversations PRIMARY KEY (id),

    CONSTRAINT fk_conversations_application
        FOREIGN KEY (application_id)
        REFERENCES applications (id),

    CONSTRAINT uk_conversations_application
        UNIQUE (application_id)
);

CREATE INDEX idx_conversations_last_message_at
    ON conversations (last_message_at DESC);


CREATE TABLE messages (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content VARCHAR(200) NOT NULL,

    CONSTRAINT pk_messages PRIMARY KEY (id),

    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES conversations (id),

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id)
        REFERENCES user_accounts (id),

    CONSTRAINT chk_messages_content_not_blank
        CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_messages_conversation_created_at
    ON messages (conversation_id, created_at);
