-- These fictional fixtures are isolated to the dev profile. Their national ID
-- HMACs use the dev-only key configured in application-dev.yaml.
-- Request pairs: 01010000001 / 29801011234567, 01110000001 /
-- 29902021234568, and 01210000001 / 30003031234569.

INSERT INTO operator (id, code, display_name, number_prefix)
SELECT fixture.id, fixture.code, fixture.display_name, fixture.number_prefix
FROM (
    SELECT 1 AS id, 'VODAFONE' AS code, 'Vodafone' AS display_name, '010' AS number_prefix
    UNION ALL
    SELECT 2, 'ETISALAT', 'Etisalat', '011'
    UNION ALL
    SELECT 3, 'ORANGE', 'Orange', '012'
) AS fixture
WHERE NOT EXISTS (
    SELECT 1
    FROM operator AS existing
    WHERE existing.id = fixture.id
      AND existing.code = fixture.code
      AND existing.number_prefix = fixture.number_prefix
);

INSERT INTO subscriber (national_id_hmac, national_id_last4, full_name)
SELECT UNHEX(fixture.hmac), fixture.last4, fixture.full_name
FROM (
    SELECT '80b2f8ab932a01c44528819e42d5991bdb888392047df1bd7e7156f8ef47a546' AS hmac,
           '4567' AS last4,
           'Test Subscriber One' AS full_name
    UNION ALL
    SELECT 'a6570ca78b271a9ae7f6e4cc4f7bda11f6e53b2d9f49dfff95ca75d53d0cf644',
           '4568',
           'Test Subscriber Two'
    UNION ALL
    SELECT '15f361d903fb34341793fd25087a9f625a7aac86df2532a9881169cd5ac859fc',
           '4569',
           'Test Subscriber Three'
    UNION ALL
    SELECT '6ca732c1dbc1763354bf479bea8bca4f6057d44c6b4b1c42f2d126243c2fcc69',
           '4560',
           'Suspended Test Subscriber'
    UNION ALL
    SELECT 'f4ec42a4eca0c081d2ab694dc3ee5c3671bcec63eb633c8dd88538e6726ab401',
           '4562',
           'Ported Test Subscriber'
) AS fixture
WHERE NOT EXISTS (
    SELECT 1
    FROM subscriber AS existing
    WHERE existing.national_id_hmac = UNHEX(fixture.hmac)
);

INSERT INTO mobile_number (
    phone_number,
    subscriber_id,
    origin_operator_id,
    current_operator_id,
    service_status,
    current_operator_since
)
SELECT fixture.phone_number,
       subscriber.id,
       origin_operator.id,
       current_operator.id,
       fixture.service_status,
       fixture.current_operator_since
FROM (
    SELECT '01010000001' AS phone_number,
           '80b2f8ab932a01c44528819e42d5991bdb888392047df1bd7e7156f8ef47a546' AS hmac,
           'VODAFONE' AS origin_code,
           'VODAFONE' AS current_code,
           'ACTIVE' AS service_status,
           TIMESTAMP('2024-01-01 00:00:00.000000') AS current_operator_since
    UNION ALL
    SELECT '01110000001',
           'a6570ca78b271a9ae7f6e4cc4f7bda11f6e53b2d9f49dfff95ca75d53d0cf644',
           'ETISALAT',
           'ETISALAT',
           'ACTIVE',
           TIMESTAMP('2024-01-01 00:00:00.000000')
    UNION ALL
    SELECT '01210000001',
           '15f361d903fb34341793fd25087a9f625a7aac86df2532a9881169cd5ac859fc',
           'ORANGE',
           'ORANGE',
           'ACTIVE',
           TIMESTAMP('2024-01-01 00:00:00.000000')
    UNION ALL
    SELECT '01010000002',
           '6ca732c1dbc1763354bf479bea8bca4f6057d44c6b4b1c42f2d126243c2fcc69',
           'VODAFONE',
           'VODAFONE',
           'SUSPENDED',
           TIMESTAMP('2024-01-01 00:00:00.000000')
    UNION ALL
    SELECT '01010000003',
           'f4ec42a4eca0c081d2ab694dc3ee5c3671bcec63eb633c8dd88538e6726ab401',
           'VODAFONE',
           'ORANGE',
           'ACTIVE',
           TIMESTAMP('2024-06-01 09:01:00.000000')
) AS fixture
JOIN subscriber
  ON subscriber.national_id_hmac = UNHEX(fixture.hmac)
JOIN operator AS origin_operator
  ON origin_operator.code = fixture.origin_code
JOIN operator AS current_operator
  ON current_operator.code = fixture.current_code
WHERE NOT EXISTS (
    SELECT 1
    FROM mobile_number AS existing
    WHERE existing.phone_number = fixture.phone_number
);

INSERT INTO porting_request (
    phone_number,
    recipient_operator_id,
    donor_operator_id,
    status,
    requested_at,
    expires_at,
    resolved_at,
    rejection_reason
)
SELECT '01010000003',
       (SELECT id FROM operator WHERE code = 'ORANGE'),
       (SELECT id FROM operator WHERE code = 'VODAFONE'),
       'ACCEPTED',
       '2024-06-01 09:00:00.000000',
       '2024-06-01 09:02:00.000000',
       '2024-06-01 09:01:00.000000',
       NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM porting_request
    WHERE phone_number = '01010000003'
      AND status = 'ACCEPTED'
);

INSERT INTO porting_request (
    phone_number,
    recipient_operator_id,
    donor_operator_id,
    status,
    requested_at,
    expires_at,
    resolved_at,
    rejection_reason
)
SELECT '01110000001',
       (SELECT id FROM operator WHERE code = 'VODAFONE'),
       (SELECT id FROM operator WHERE code = 'ETISALAT'),
       'REJECTED',
       '2025-01-01 10:00:00.000000',
       '2025-01-01 10:02:00.000000',
       '2025-01-01 10:01:00.000000',
       'OUTSTANDING_BALANCE'
WHERE NOT EXISTS (
    SELECT 1
    FROM porting_request
    WHERE phone_number = '01110000001'
      AND status = 'REJECTED'
);
