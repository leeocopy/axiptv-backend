CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    device_hash TEXT UNIQUE NOT NULL,
    first_seen_at TIMESTAMPTZ DEFAULT NOW(),
    last_seen_at TIMESTAMPTZ DEFAULT NOW(),
    trial_end_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    active_until TIMESTAMPTZ NULL,
    note TEXT NULL,
    blocked BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_devices_last_seen_at ON devices (last_seen_at DESC);
