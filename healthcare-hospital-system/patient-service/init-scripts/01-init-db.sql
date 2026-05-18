CREATE TABLE IF NOT EXISTS patient (
 id BINARY(16) PRIMARY KEY,
 first_name VARCHAR(100) NOT NULL,
 last_name VARCHAR(100),
 age INT NOT NULL,
 gender VARCHAR(20) NOT NULL,
 email VARCHAR(150) NOT NULL UNIQUE,
 phone_number VARCHAR(20),
 created_at DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS patient_address (
 id BINARY(16) PRIMARY KEY,
 country VARCHAR(100) NOT NULL,
 state VARCHAR(100),
 house_name VARCHAR(150),
 CONSTRAINT fk_patient_address_patient FOREIGN KEY (id) REFERENCES patient(id) ON DELETE CASCADE
);
INSERT INTO patient (
  id,
  first_name,
  last_name,
  age,
  gender,
  email,
  phone_number,
  created_at
 )
VALUES (
  UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),
  'Rahul',
  'Sharma',
  32,
  'MALE',
  'rahul.sharma@example.com',
  '9876543210',
  NOW()
 ),
 (
  UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
  'Ananya',
  'Rao',
  28,
  'FEMALE',
  'ananya.rao@example.com',
  '9876501234',
  NOW()
 ),
 (
  UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
  'David',
  'Thomas',
  45,
  'MALE',
  'david.thomas@example.com',
  '9988776655',
  NOW()
 );
INSERT INTO patient_address (
  id,
  country,
  state,
  house_name
 )
VALUES (
  UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),
  'India',
  'Karnataka',
  'Green Villa'
 ),
 (
  UUID_TO_BIN('22222222-2222-2222-2222-222222222222'),
  'India',
  'Kerala',
  'Blue Nest'
 ),
 (
  UUID_TO_BIN('33333333-3333-3333-3333-333333333333'),
  'India',
  'Tamil Nadu',
  'Sunrise Apartments'
 );