CREATE TABLE IF NOT EXISTS payment (
    id                  BINARY(16)      PRIMARY KEY,
    appointment_id      BINARY(16)      NOT NULL,
    patient_id          BINARY(16)      NOT NULL,
    amount              DECIMAL(10, 2)  NOT NULL,
    currency            VARCHAR(10)     NOT NULL DEFAULT 'INR',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME
);
