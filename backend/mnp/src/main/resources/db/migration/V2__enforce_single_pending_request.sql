ALTER TABLE porting_request
    ADD COLUMN pending_phone_number CHAR(11)
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'PENDING' THEN phone_number
                ELSE NULL
                END
            ) STORED,
    ADD CONSTRAINT uk_pr_one_pending_per_phone
        UNIQUE (pending_phone_number);