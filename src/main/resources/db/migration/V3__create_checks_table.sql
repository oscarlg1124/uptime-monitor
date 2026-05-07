CREATE TABLE checks (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    monitor_id      UUID        NOT NULL REFERENCES monitors (id) ON DELETE CASCADE,
    status_code     INT,
    response_time_ms BIGINT,
    is_up           BOOLEAN     NOT NULL,
    checked_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_checks_monitor_id ON checks (monitor_id);
CREATE INDEX idx_checks_checked_at ON checks (checked_at DESC);
