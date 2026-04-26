CREATE TABLE IF NOT EXISTS search_execution_metrics (
    id BIGSERIAL PRIMARY KEY,
    locale VARCHAR(10),
    view_mode VARCHAR(20) NOT NULL,
    result_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_execution_metrics_created_at
    ON search_execution_metrics(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_search_execution_metrics_view_mode
    ON search_execution_metrics(view_mode);
