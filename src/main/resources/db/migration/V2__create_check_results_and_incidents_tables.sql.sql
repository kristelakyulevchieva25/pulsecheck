CREATE TABLE check_results (
    id BIGSERIAL PRIMARY KEY,
    status_code INT,
    response_time_ms BIGINT,
    success BOOLEAN,
    error_message TEXT,
    checked_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    monitor_id BIGINT NOT NULL,
    CONSTRAINT fk_check_results_monitor FOREIGN KEY (monitor_id) REFERENCES monitors(id) ON DELETE CASCADE
);

CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    cause TEXT,
    started_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITHOUT TIME ZONE,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    escalated BOOLEAN DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    monitor_id BIGINT NOT NULL,
    CONSTRAINT fk_incidents_monitor FOREIGN KEY (monitor_id) REFERENCES monitors(id) ON DELETE CASCADE
);

CREATE INDEX idx_check_results_monitor_id ON check_results(monitor_id);
CREATE INDEX idx_incidents_monitor_id ON incidents(monitor_id);