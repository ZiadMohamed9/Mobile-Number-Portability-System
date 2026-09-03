-- ==============================================================================
-- Mobile Number Portability (MNP) System - Database Initialization Script
-- Engine: MySQL 8.0+ / 8.4+
-- Charset: utf8mb4
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS porting_request;
DROP TABLE IF EXISTS mobile_number;
DROP TABLE IF EXISTS subscriber;
DROP TABLE IF EXISTS operator;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------------------------
-- 1. Operator Table
-- Stores telecommunication service providers (e.g., Vodafone, Etisalat, Orange)
-- ------------------------------------------------------------------------------
CREATE TABLE operator (
    id            SMALLINT     NOT NULL,
    code          VARCHAR(20)  NOT NULL,
    display_name  VARCHAR(50)  NOT NULL,
    number_prefix CHAR(3)      NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_operator_code   UNIQUE (code),
    CONSTRAINT uk_operator_prefix UNIQUE (number_prefix)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 2. Subscriber Table
-- Stores subscriber identity information with HMAC hashing for privacy protection
-- ------------------------------------------------------------------------------
CREATE TABLE subscriber (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    national_id_hmac BINARY(32)   NOT NULL,
    national_id_last4 CHAR(4)     NOT NULL,
    full_name        VARCHAR(150) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_subscriber_hmac UNIQUE (national_id_hmac)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 3. Mobile Number Table
-- Tracks phone numbers, owner subscription, origin & current operators, and service status
-- ------------------------------------------------------------------------------
CREATE TABLE mobile_number (
    phone_number           CHAR(11)    NOT NULL,
    subscriber_id          BIGINT      NOT NULL,
    origin_operator_id     SMALLINT    NOT NULL,
    current_operator_id    SMALLINT    NOT NULL,
    service_status         VARCHAR(20) NOT NULL,
    current_operator_since DATETIME(6) NOT NULL,

    PRIMARY KEY (phone_number),
    CONSTRAINT fk_mn_subscriber  FOREIGN KEY (subscriber_id)       REFERENCES subscriber (id),
    CONSTRAINT fk_mn_origin_op   FOREIGN KEY (origin_operator_id)  REFERENCES operator (id),
    CONSTRAINT fk_mn_current_op  FOREIGN KEY (current_operator_id) REFERENCES operator (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 4. Porting Request Table
-- Records MNP porting requests between recipient and donor operators
-- Includes stored generated column to strictly enforce at most one PENDING request per number
-- ------------------------------------------------------------------------------
CREATE TABLE porting_request (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    phone_number          CHAR(11)     NOT NULL,
    recipient_operator_id SMALLINT     NOT NULL,
    donor_operator_id     SMALLINT     NOT NULL,
    status                VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    requested_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expires_at            DATETIME(6)  NOT NULL DEFAULT (CURRENT_TIMESTAMP(6) + INTERVAL 2 MINUTE),
    resolved_at           DATETIME(6)  NULL,
    rejection_reason      VARCHAR(500) NULL,
    pending_phone_number  CHAR(11)     GENERATED ALWAYS AS (
                            CASE WHEN status = 'PENDING' THEN phone_number ELSE NULL END
                          ) STORED,

    PRIMARY KEY (id),
    CONSTRAINT fk_pr_phone        FOREIGN KEY (phone_number)          REFERENCES mobile_number (phone_number),
    CONSTRAINT fk_pr_recipient_op FOREIGN KEY (recipient_operator_id) REFERENCES operator (id),
    CONSTRAINT fk_pr_donor_op     FOREIGN KEY (donor_operator_id)     REFERENCES operator (id),
    CONSTRAINT uk_pr_one_pending_per_phone UNIQUE (pending_phone_number),
    INDEX idx_pr_phone_status (phone_number, status),
    INDEX idx_pr_status_expires_at (status, expires_at),
    INDEX idx_pr_status_resolved_at (status, resolved_at DESC),
    INDEX idx_pr_recipient_requested_at (recipient_operator_id, requested_at DESC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- ==============================================================================
-- Seed Data Insertion
-- ==============================================================================

-- 1. Insert Operators
INSERT INTO operator (id, code, display_name, number_prefix) VALUES
(1, 'VODAFONE', 'Vodafone', '010'),
(2, 'ETISALAT', 'Etisalat', '011'),
(3, 'ORANGE',   'Orange',   '012');

-- 2. Insert Subscribers
INSERT INTO subscriber (id, national_id_hmac, national_id_last4, full_name) VALUES
(1, UNHEX('80b2f8ab932a01c44528819e42d5991bdb888392047df1bd7e7156f8ef47a546'), '4567', 'Test Subscriber One'),
(2, UNHEX('a6570ca78b271a9ae7f6e4cc4f7bda11f6e53b2d9f49dfff95ca75d53d0cf644'), '4568', 'Test Subscriber Two'),
(3, UNHEX('15f361d903fb34341793fd25087a9f625a7aac86df2532a9881169cd5ac859fc'), '4569', 'Test Subscriber Three'),
(4, UNHEX('6ca732c1dbc1763354bf479bea8bca4f6057d44c6b4b1c42f2d126243c2fcc69'), '4560', 'Suspended Test Subscriber'),
(5, UNHEX('f4ec42a4eca0c081d2ab694dc3ee5c3671bcec63eb633c8dd88538e6726ab401'), '4562', 'Ported Test Subscriber');

-- 3. Insert Mobile Numbers
INSERT INTO mobile_number (phone_number, subscriber_id, origin_operator_id, current_operator_id, service_status, current_operator_since) VALUES
('01010000001', 1, 1, 1, 'ACTIVE',    '2024-01-01 00:00:00.000000'),
('01110000001', 2, 2, 2, 'ACTIVE',    '2024-01-01 00:00:00.000000'),
('01210000001', 3, 3, 3, 'ACTIVE',    '2024-01-01 00:00:00.000000'),
('01010000002', 4, 1, 1, 'SUSPENDED', '2024-01-01 00:00:00.000000'),
('01010000003', 5, 1, 3, 'ACTIVE',    '2024-06-01 09:01:00.000000');

-- 4. Insert Porting Requests
INSERT INTO porting_request (phone_number, recipient_operator_id, donor_operator_id, status, requested_at, expires_at, resolved_at, rejection_reason) VALUES
('01010000003', 3, 1, 'ACCEPTED', '2024-06-01 09:00:00.000000', '2024-06-01 09:02:00.000000', '2024-06-01 09:01:00.000000', NULL),
('01110000001', 1, 2, 'REJECTED', '2025-01-01 10:00:00.000000', '2025-01-01 10:02:00.000000', '2025-01-01 10:01:00.000000', 'OUTSTANDING_BALANCE'),
('01010000001', 2, 1, 'PENDING',  NOW(6),                      NOW(6) + INTERVAL 2 MINUTE,    NULL,                         NULL);
