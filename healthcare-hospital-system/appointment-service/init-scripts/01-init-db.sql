-- ============================================================
-- Appointment table
-- ============================================================
CREATE TABLE IF NOT EXISTS appointment (
    id                      BINARY(16)      PRIMARY KEY,
    patient_id              BINARY(16)      NOT NULL,
    doctor_id               BINARY(16)      NOT NULL,
    slot_id                 BINARY(16)      NOT NULL,
    status                  VARCHAR(30)     NOT NULL,
    reason                  VARCHAR(255)    NOT NULL,
    notes                   VARCHAR(500),
    appointment_date_time   DATETIME,
    created_at              DATETIME        NOT NULL,
    updated_at              DATETIME
);

-- ============================================================
-- Seed: 2 Appointments
--
-- Patient UUIDs match patient-service init-scripts/01-init-db.sql:
--   11111111-1111-1111-1111-111111111111  →  Rahul Sharma
--   22222222-2222-2222-2222-222222222222  →  Ananya Rao
--
-- Doctor UUIDs match doctor-service init-script/01-init-db.sql:
--   aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa  →  Dr. Arun Kumar   (Cardiology)
--   bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb  →  Dr. Priya Nair   (Dermatology)
--
-- Slot UUIDs match doctor_slots rows whose reserved_by_appointment_id
-- equals these appointment IDs, keeping all three DBs in sync:
--   11111111-0000-0000-0000-000000000002  →  Dr. Arun Kumar slot  (RESERVED)
--   22222222-0000-0000-0000-000000000003  →  Dr. Priya Nair slot  (RESERVED)
-- ============================================================
INSERT INTO appointment (
    id,
    patient_id,
    doctor_id,
    slot_id,
    status,
    reason,
    notes,
    appointment_date_time,
    created_at,
    updated_at
)
VALUES
    (
        UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01'),
        UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),   -- Rahul Sharma
        UUID_TO_BIN('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),   -- Dr. Arun Kumar (Cardiology)
        UUID_TO_BIN('11111111-0000-0000-0000-000000000002'),   -- slot 2026-06-01 10:00
        'SLOT_RESERVED',
        'Chest pain and shortness of breath',
        NULL,
        '2026-06-01 10:00:00',
        NOW(),
        NULL
    ),
    (
        UUID_TO_BIN('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03'),
        UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),   -- Ananya Rao
        UUID_TO_BIN('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),   -- Dr. Priya Nair (Dermatology)
        UUID_TO_BIN('22222222-0000-0000-0000-000000000003'),   -- slot 2026-06-02 11:00
        'SLOT_RESERVED',
        'Recurring skin rash on arms',
        NULL,
        '2026-06-02 11:00:00',
        NOW(),
        NULL
    );
