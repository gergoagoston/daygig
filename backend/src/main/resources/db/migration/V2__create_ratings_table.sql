CREATE TABLE ratings (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    job_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    reviewee_id UUID NOT NULL,
    context VARCHAR(255) NOT NULL,
    stars INTEGER NOT NULL,
    comment VARCHAR(500),

    CONSTRAINT pk_ratings PRIMARY KEY (id),

    CONSTRAINT fk_ratings_job
        FOREIGN KEY (job_id)
        REFERENCES jobs (id),

    CONSTRAINT fk_ratings_reviewer
        FOREIGN KEY (reviewer_id)
        REFERENCES user_accounts (id),

    CONSTRAINT fk_ratings_reviewee
        FOREIGN KEY (reviewee_id)
        REFERENCES user_accounts (id),

    CONSTRAINT uk_ratings_job_reviewer
        UNIQUE (job_id, reviewer_id),

    CONSTRAINT chk_ratings_context
        CHECK (context IN ('WORKER', 'JOB_OWNER')),

    CONSTRAINT chk_ratings_stars
        CHECK (stars BETWEEN 1 AND 5),

    CONSTRAINT chk_ratings_different_users
        CHECK (reviewer_id <> reviewee_id)
);

CREATE INDEX idx_ratings_reviewee_context_created_at
    ON ratings (reviewee_id, context, created_at);
