CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE OR REPLACE FUNCTION calculate_distance_km(
    first_latitude NUMERIC,
    first_longitude NUMERIC,
    second_latitude NUMERIC,
    second_longitude NUMERIC
)
RETURNS DOUBLE PRECISION
LANGUAGE SQL
IMMUTABLE
PARALLEL SAFE
RETURN ST_DistanceSphere(
    ST_SetSRID(
        ST_MakePoint(
            first_longitude::DOUBLE PRECISION,
            first_latitude::DOUBLE PRECISION
        ),
        4326
    ),
    ST_SetSRID(
        ST_MakePoint(
            second_longitude::DOUBLE PRECISION,
            second_latitude::DOUBLE PRECISION
        ),
        4326
    )
) / 1000.0;


CREATE TABLE user_accounts (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    bio VARCHAR(1000),
    terms_accepted_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    is_deleted BOOLEAN NOT NULL,

    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT chk_user_accounts_role
        CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

CREATE UNIQUE INDEX idx_users_email
    ON user_accounts (email);


CREATE TABLE jobs (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    title VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    hourly_rate NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    owner_id UUID NOT NULL,
    completed_application_id UUID,
    completed_at TIMESTAMP(6) WITHOUT TIME ZONE,

    CONSTRAINT pk_jobs PRIMARY KEY (id),

    CONSTRAINT fk_jobs_owner
        FOREIGN KEY (owner_id)
        REFERENCES user_accounts (id),

    CONSTRAINT uk_jobs_completed_application
        UNIQUE (completed_application_id),

    CONSTRAINT chk_jobs_category
        CHECK (
            category IN (
                'CLEANING',
                'MOVING',
                'GARDENING',
                'DELIVERY',
                'BABYSITTING',
                'PET_CARE',
                'HANDYMAN',
                'EVENT_HELP',
                'OTHER'
            )
        ),

    CONSTRAINT chk_jobs_status
        CHECK (status IN ('OPEN', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT chk_jobs_currency
        CHECK (currency IN ('RON'))
);

CREATE INDEX idx_jobs_status_created_at
    ON jobs (status, created_at);

CREATE INDEX idx_jobs_owner_id
    ON jobs (owner_id);


CREATE TABLE applications (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    applicant_id UUID NOT NULL,
    job_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,

    CONSTRAINT pk_applications PRIMARY KEY (id),

    CONSTRAINT fk_applications_applicant
        FOREIGN KEY (applicant_id)
        REFERENCES user_accounts (id),

    CONSTRAINT fk_applications_job
        FOREIGN KEY (job_id)
        REFERENCES jobs (id),

    CONSTRAINT uk_applications_applicant_job
        UNIQUE (applicant_id, job_id),

    CONSTRAINT chk_applications_status
        CHECK (status IN ('ACTIVE', 'REJECTED', 'WITHDRAWN'))
);

CREATE INDEX idx_applications_applicant_id
    ON applications (applicant_id);

CREATE INDEX idx_applications_job_id
    ON applications (job_id);


ALTER TABLE jobs
    ADD CONSTRAINT fk_jobs_completed_application
    FOREIGN KEY (completed_application_id)
    REFERENCES applications (id);


CREATE TABLE user_avatars (
    user_id UUID NOT NULL,
    image_data BYTEA NOT NULL,
    content_type VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT pk_user_avatars PRIMARY KEY (user_id),

    CONSTRAINT fk_user_avatars_user
        FOREIGN KEY (user_id)
        REFERENCES user_accounts (id)
);
