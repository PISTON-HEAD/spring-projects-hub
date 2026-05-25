-- ============================================================
-- Doctor table
-- ============================================================
CREATE TABLE IF NOT EXISTS doctor (
    id              BINARY(16)      PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100),
    specialization  VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    phone_number    VARCHAR(20),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL
);

-- ============================================================
-- Doctor slots table
-- ============================================================
CREATE TABLE IF NOT EXISTS doctor_slots (
    id                          BINARY(16)  PRIMARY KEY,
    doctor_id                   BINARY(16)  NOT NULL,
    start_time                  DATETIME    NOT NULL,
    end_time                    DATETIME    NOT NULL,
    status                      VARCHAR(20) NOT NULL,
    reserved_by_appointment_id  BINARY(16),
    created_at                  DATETIME    NOT NULL,
    CONSTRAINT fk_slot_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    CONSTRAINT doctor_reservation_uniqueness
        UNIQUE (doctor_id, start_time, end_time)
);

-- ============================================================
-- Seed: Doctors
-- ============================================================
INSERT INTO doctor (id, first_name, last_name, specialization, email, phone_number, active, created_at)
VALUES
    (UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), 'Arun',   'Kumar',  'Cardiology',     'arun.kumar@hospital.com',   '9876541001', TRUE, NOW()),
    (UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'), 'Priya',  'Nair',   'Dermatology',    'priya.nair@hospital.com',   '9876541002', TRUE, NOW()),
    (UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'), 'Vikram', 'Mehta',  'Orthopedics',    'vikram.mehta@hospital.com', '9876541003', TRUE, NOW()),
    (UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'), 'Sneha',  'Patel',  'Neurology',      'sneha.patel@hospital.com',  '9876541004', TRUE, NOW());

-- ============================================================
-- Seed: Doctor Slots
-- Each doctor gets 3 slots — mix of AVAILABLE, RESERVED, CONFIRMED
-- ============================================================
INSERT INTO doctor_slots (id, doctor_id, start_time, end_time, status, reserved_by_appointment_id, created_at)
VALUES
    -- Dr. Arun Kumar (Cardiology)
    (UUID_TO_BIN('11111111-0000-0000-0000-000000000001'), UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), '2026-06-01 09:00:00', '2026-06-01 09:30:00', 'AVAILABLE',  NULL,                                          NOW()),
    (UUID_TO_BIN('11111111-0000-0000-0000-000000000002'), UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), '2026-06-01 10:00:00', '2026-06-01 10:30:00', 'RESERVED',   UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01'), NOW()),
    (UUID_TO_BIN('11111111-0000-0000-0000-000000000003'), UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'), '2026-06-01 11:00:00', '2026-06-01 11:30:00', 'CONFIRMED',  UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02'), NOW()),

    -- Dr. Priya Nair (Dermatology)
    (UUID_TO_BIN('22222222-0000-0000-0000-000000000001'), UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'), '2026-06-02 09:00:00', '2026-06-02 09:30:00', 'AVAILABLE',  NULL,                                          NOW()),
    (UUID_TO_BIN('22222222-0000-0000-0000-000000000002'), UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'), '2026-06-02 10:00:00', '2026-06-02 10:30:00', 'AVAILABLE',  NULL,                                          NOW()),
    (UUID_TO_BIN('22222222-0000-0000-0000-000000000003'), UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'), '2026-06-02 11:00:00', '2026-06-02 11:30:00', 'RESERVED',   UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03'), NOW()),

    -- Dr. Vikram Mehta (Orthopedics)
    (UUID_TO_BIN('33333333-0000-0000-0000-000000000001'), UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'), '2026-06-03 09:00:00', '2026-06-03 09:30:00', 'CONFIRMED',  UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04'), NOW()),
    (UUID_TO_BIN('33333333-0000-0000-0000-000000000002'), UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'), '2026-06-03 10:00:00', '2026-06-03 10:30:00', 'AVAILABLE',  NULL,                                          NOW()),
    (UUID_TO_BIN('33333333-0000-0000-0000-000000000003'), UUID_TO_BIN('cccccccc-cccc-cccc-cccc-cccccccccccc'), '2026-06-03 11:00:00', '2026-06-03 11:30:00', 'AVAILABLE',  NULL,                                          NOW()),

    -- Dr. Sneha Patel (Neurology)
    (UUID_TO_BIN('44444444-0000-0000-0000-000000000001'), UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'), '2026-06-04 09:00:00', '2026-06-04 09:30:00', 'AVAILABLE',  NULL,                                          NOW()),
    (UUID_TO_BIN('44444444-0000-0000-0000-000000000002'), UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'), '2026-06-04 10:00:00', '2026-06-04 10:30:00', 'RESERVED',   UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee05'), NOW()),
    (UUID_TO_BIN('44444444-0000-0000-0000-000000000003'), UUID_TO_BIN('dddddddd-dddd-dddd-dddd-dddddddddddd'), '2026-06-04 11:00:00', '2026-06-04 11:30:00', 'AVAILABLE',  NULL,                                          NOW());
