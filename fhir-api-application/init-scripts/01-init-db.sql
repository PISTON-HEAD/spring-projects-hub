-- MySQL Initialization Script for FHIR Patient Database
-- This script runs automatically when the MySQL container starts

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS fhir_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fhir_db;

-- Create Users table
CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Patient table
CREATE TABLE IF NOT EXISTS patient (
    id CHAR(36) NOT NULL PRIMARY KEY,
    first_name VARCHAR(255),
    middle_name VARCHAR(255),
    last_name VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    postal_code VARCHAR(255),
    country VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Patient Identifier table
CREATE TABLE IF NOT EXISTS patient_identifier (
    identifier_id CHAR(36) NOT NULL PRIMARY KEY,
    patient_id CHAR(36) NOT NULL,
    identifier_type VARCHAR(255) NOT NULL,
    identifier_value VARCHAR(255) NOT NULL,
    `system` VARCHAR(255),
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_patient_identifier FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert Sample Users (password: "password" with BCrypt hash)
INSERT INTO users (id, username, password, email, role, active, created_at, updated_at) 
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@fhir.com', 'ROLE_USER', TRUE, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440002', 'doctor1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'doctor1@fhir.com', 'ROLE_USER', TRUE, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440003', 'nurse1', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'nurse1@fhir.com', 'ROLE_USER', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Insert Sample Patients
INSERT INTO patient (id, first_name, middle_name, last_name, date_of_birth, gender, phone, email, address_line1, address_line2, city, state, postal_code, country, active, created_at, updated_at) 
VALUES 
    ('650e8400-e29b-41d4-a716-446655440001', 'John', 'Michael', 'Doe', '1990-01-15', 'male', '555-0101', 'john.doe@email.com', '123 Main Street', 'Apt 4B', 'New York', 'NY', '10001', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440002', 'Jane', 'Elizabeth', 'Smith', '1985-05-20', 'female', '555-0102', 'jane.smith@email.com', '456 Oak Avenue', NULL, 'Los Angeles', 'CA', '90001', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440003', 'Robert', 'James', 'Johnson', '1978-11-30', 'male', '555-0103', 'robert.johnson@email.com', '789 Pine Road', 'Suite 200', 'Chicago', 'IL', '60601', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440004', 'Emily', 'Rose', 'Williams', '1992-03-25', 'female', '555-0104', 'emily.williams@email.com', '321 Elm Street', NULL, 'Houston', 'TX', '77001', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440005', 'Michael', 'David', 'Brown', '1988-07-10', 'male', '555-0105', 'michael.brown@email.com', '654 Maple Drive', 'Unit 5', 'Phoenix', 'AZ', '85001', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440006', 'Sarah', 'Anne', 'Davis', '1995-12-05', 'female', '555-0106', 'sarah.davis@email.com', '987 Cedar Lane', NULL, 'Philadelphia', 'PA', '19101', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440007', 'David', 'Lee', 'Martinez', '1982-09-18', 'male', '555-0107', 'david.martinez@email.com', '147 Birch Court', 'Apt 12', 'San Antonio', 'TX', '78201', 'USA', TRUE, NOW(), NOW()),
    ('650e8400-e29b-41d4-a716-446655440008', 'Lisa', 'Marie', 'Garcia', '1991-02-28', 'female', '555-0108', 'lisa.garcia@email.com', '258 Spruce Boulevard', NULL, 'San Diego', 'CA', '92101', 'USA', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE first_name=first_name;

-- Insert Patient Identifiers
INSERT INTO patient_identifier (identifier_id, patient_id, identifier_type, identifier_value, `system`, created_at) 
VALUES 
    -- John Doe identifiers
    ('750e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'MRN', 'MRN0001', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 'AccountNumber', 'ACN0001', 'http://hospital.com/account', NOW()),
    ('750e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 'VisitNumber', 'VIS0001', 'http://hospital.com/visit', NOW()),
    ('750e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', 'DrivingLicense', 'DL12345', 'http://dmv.state.gov', NOW()),
    
    -- Jane Smith identifiers
    ('750e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440002', 'MRN', 'MRN0002', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440006', '650e8400-e29b-41d4-a716-446655440002', 'AccountNumber', 'ACN0002', 'http://hospital.com/account', NOW()),
    ('750e8400-e29b-41d4-a716-446655440007', '650e8400-e29b-41d4-a716-446655440002', 'SSN', 'SSN123456', 'http://ssa.gov', NOW()),
    
    -- Robert Johnson identifiers
    ('750e8400-e29b-41d4-a716-446655440008', '650e8400-e29b-41d4-a716-446655440003', 'MRN', 'MRN0003', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440009', '650e8400-e29b-41d4-a716-446655440003', 'AccountNumber', 'ACN0003', 'http://hospital.com/account', NOW()),
    ('750e8400-e29b-41d4-a716-446655440010', '650e8400-e29b-41d4-a716-446655440003', 'VisitNumber', 'VIS0003', 'http://hospital.com/visit', NOW()),
    
    -- Emily Williams identifiers
    ('750e8400-e29b-41d4-a716-446655440011', '650e8400-e29b-41d4-a716-446655440004', 'MRN', 'MRN0004', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440012', '650e8400-e29b-41d4-a716-446655440004', 'AccountNumber', 'ACN0004', 'http://hospital.com/account', NOW()),
    
    -- Michael Brown identifiers
    ('750e8400-e29b-41d4-a716-446655440013', '650e8400-e29b-41d4-a716-446655440005', 'MRN', 'MRN0005', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440014', '650e8400-e29b-41d4-a716-446655440005', 'AccountNumber', 'ACN0005', 'http://hospital.com/account', NOW()),
    ('750e8400-e29b-41d4-a716-446655440015', '650e8400-e29b-41d4-a716-446655440005', 'InsuranceID', 'INS54321', 'http://insurance.com', NOW()),
    
    -- Sarah Davis identifiers
    ('750e8400-e29b-41d4-a716-446655440016', '650e8400-e29b-41d4-a716-446655440006', 'MRN', 'MRN0006', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440017', '650e8400-e29b-41d4-a716-446655440006', 'AccountNumber', 'ACN0006', 'http://hospital.com/account', NOW()),
    
    -- David Martinez identifiers
    ('750e8400-e29b-41d4-a716-446655440018', '650e8400-e29b-41d4-a716-446655440007', 'MRN', 'MRN0007', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440019', '650e8400-e29b-41d4-a716-446655440007', 'AccountNumber', 'ACN0007', 'http://hospital.com/account', NOW()),
    ('750e8400-e29b-41d4-a716-446655440020', '650e8400-e29b-41d4-a716-446655440007', 'VisitNumber', 'VIS0007', 'http://hospital.com/visit', NOW()),
    
    -- Lisa Garcia identifiers
    ('750e8400-e29b-41d4-a716-446655440021', '650e8400-e29b-41d4-a716-446655440008', 'MRN', 'MRN0008', 'http://hospital.com/mrn', NOW()),
    ('750e8400-e29b-41d4-a716-446655440022', '650e8400-e29b-41d4-a716-446655440008', 'AccountNumber', 'ACN0008', 'http://hospital.com/account', NOW())
ON DUPLICATE KEY UPDATE identifier_value=identifier_value;

-- Verification output
SELECT 'Database initialization completed!' AS status;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS patient_count FROM patient;
SELECT COUNT(*) AS identifier_count FROM patient_identifier;
