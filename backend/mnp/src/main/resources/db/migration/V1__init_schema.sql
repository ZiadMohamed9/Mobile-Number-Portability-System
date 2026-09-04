CREATE TABLE operator
(
    id            SMALLINT    NOT NULL,
    code          VARCHAR(20) NOT NULL,
    display_name  VARCHAR(50) NOT NULL,
    number_prefix CHAR(3)     NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_operator_code   UNIQUE (code),
    CONSTRAINT uk_operator_prefix UNIQUE (number_prefix)
) ENGINE = InnoDB;

CREATE TABLE subscriber
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    national_id_hmac BINARY(32)   NOT NULL,
    national_id_last4 CHAR(4)     NOT NULL,
    full_name        VARCHAR(150) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_subscriber_hmac UNIQUE (national_id_hmac)
) ENGINE = InnoDB;

CREATE TABLE mobile_number
(
    phone_number          CHAR(11)    NOT NULL,
    subscriber_id         BIGINT      NOT NULL,
    origin_operator_id    SMALLINT    NOT NULL,
    current_operator_id   SMALLINT    NOT NULL,
    service_status        VARCHAR(20) NOT NULL,
    current_operator_since DATETIME(6) NOT NULL,

    PRIMARY KEY (phone_number),
    CONSTRAINT fk_mn_subscriber  FOREIGN KEY (subscriber_id)       REFERENCES subscriber (id),
    CONSTRAINT fk_mn_origin_op   FOREIGN KEY (origin_operator_id)  REFERENCES operator (id),
    CONSTRAINT fk_mn_current_op  FOREIGN KEY (current_operator_id) REFERENCES operator (id)
) ENGINE = InnoDB;

CREATE TABLE porting_request
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    phone_number          CHAR(11)     NOT NULL,
    recipient_operator_id SMALLINT     NOT NULL,
    donor_operator_id     SMALLINT     NOT NULL,
    status                VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    requested_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at            DATETIME(6)  NOT NULL DEFAULT (CURRENT_TIMESTAMP(6) + INTERVAL 2 MINUTE),
    resolved_at           DATETIME(6)  NULL,
    rejection_reason      VARCHAR(500) NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_pr_phone        FOREIGN KEY (phone_number)          REFERENCES mobile_number (phone_number),
    CONSTRAINT fk_pr_recipient_op FOREIGN KEY (recipient_operator_id) REFERENCES operator (id),
    CONSTRAINT fk_pr_donor_op     FOREIGN KEY (donor_operator_id)     REFERENCES operator (id),
    INDEX idx_pr_phone_status (phone_number, status)
) ENGINE = InnoDB;
