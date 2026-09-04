CREATE INDEX idx_pr_status_expires_at
    ON porting_request (status, expires_at);

CREATE INDEX idx_pr_status_resolved_at
    ON porting_request (status, resolved_at DESC);

CREATE INDEX idx_pr_recipient_requested_at
    ON porting_request (recipient_operator_id, requested_at DESC);