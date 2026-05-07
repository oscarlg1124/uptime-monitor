CREATE TABLE monitors (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    url             VARCHAR(2048) NOT NULL,
    name            VARCHAR(255)  NOT NULL,
    interval_seconds INT          NOT NULL DEFAULT 300,
    status          VARCHAR(10)   NOT NULL DEFAULT 'PENDING',
    last_checked_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_monitors_user_id ON monitors (user_id);
CREATE INDEX idx_monitors_status  ON monitors (status);
