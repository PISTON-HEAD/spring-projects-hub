# Docker Database Initialization Guide

## Overview

The MySQL database is automatically initialized with sample data when you start the Docker container for the first time.

## How It Works

### Initialization Script

- **Location**: `init-scripts/01-init-db.sql`
- **Execution**: Runs automatically when MySQL container starts **for the first time**
- **Trigger**: Docker volume mount at `/docker-entrypoint-initdb.d`

### What Gets Created

#### 1. Database Schema

- **Database**: `fhir_db`
- **Tables**:
  - `users` - Authentication users
  - `patient` - Patient demographics
  - `patient_identifier` - Patient identifiers (MRN, SSN, etc.)

#### 2. Sample Users (3 users)

All users have the password: `password`

| Username | Email            | Role      |
| -------- | ---------------- | --------- |
| admin    | admin@fhir.com   | ROLE_USER |
| doctor1  | doctor1@fhir.com | ROLE_USER |
| nurse1   | nurse1@fhir.com  | ROLE_USER |

#### 3. Sample Patients (8 patients)

| Name                 | DOB        | Gender | City         | State | MRN     |
| -------------------- | ---------- | ------ | ------------ | ----- | ------- |
| John Michael Doe     | 1990-01-15 | male   | New York     | NY    | MRN0001 |
| Jane Elizabeth Smith | 1985-05-20 | female | Los Angeles  | CA    | MRN0002 |
| Robert James Johnson | 1978-11-30 | male   | Chicago      | IL    | MRN0003 |
| Emily Rose Williams  | 1992-03-25 | female | Houston      | TX    | MRN0004 |
| Michael David Brown  | 1988-07-10 | male   | Phoenix      | AZ    | MRN0005 |
| Sarah Anne Davis     | 1995-12-05 | female | Philadelphia | PA    | MRN0006 |
| David Lee Martinez   | 1982-09-18 | male   | San Antonio  | TX    | MRN0007 |
| Lisa Marie Garcia    | 1991-02-28 | female | San Diego    | CA    | MRN0008 |

#### 4. Patient Identifiers (22 identifiers)

Each patient has 2-4 identifiers including:

- **MRN** (Medical Record Number) - All patients
- **AccountNumber** - All patients
- **VisitNumber** - Selected patients
- **SSN** - Selected patients
- **DrivingLicense** - Selected patients
- **InsuranceID** - Selected patients

## Usage

### First Time Setup

```bash
# 1. Start MySQL container with initialization
docker-compose up -d

# 2. Wait for MySQL to initialize (check logs)
docker-compose logs -f mysql

# 3. Look for these messages:
#    - "Database initialization completed!"
#    - "MySQL init process done. Ready for start up."

# 4. Verify data was loaded
docker exec -it fhir-mysql mysql -uroot -proot -e "USE fhir_db; SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM patient;"
```

**Expected Output:**

```
COUNT(*)
3

COUNT(*)
8
```

### Starting Spring Boot Application

After Docker initialization completes:

```bash
# Start the application
mvn spring-boot:run
```

The application will:

- ✅ Connect to MySQL
- ✅ Use existing schema (Hibernate `ddl-auto: update`)
- ✅ Keep all init data intact
- ✅ Be ready to accept API requests

## Important Notes

### ⚠️ Initialization Only Happens Once

The init script runs **only when the Docker volume is created for the first time**. If you want to reinitialize:

```bash
# Stop and remove containers and volumes
docker-compose down -v

# Start fresh (this will run init script again)
docker-compose up -d
```

### ⚠️ Hibernate Configuration

The application uses `ddl-auto: update` instead of `create-drop` to preserve the Docker init data:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # Preserves existing data
```

**Don't change this to `create-drop`** or the init data will be lost when Spring Boot starts!

### ⚠️ Data Persistence

Data persists in the Docker volume `mysql-data` even when you stop the container:

```bash
# Stop container - data remains
docker-compose down

# Start container - data still there
docker-compose up -d
```

## Verification Commands

### Check MySQL Container Status

```bash
docker-compose ps
```

### View Initialization Logs

```bash
docker-compose logs mysql | grep -i "init"
```

### Connect to MySQL CLI

```bash
docker exec -it fhir-mysql mysql -uroot -proot fhir_db
```

### Verify Data in MySQL

```sql
-- Check users
SELECT username, email, role FROM users;

-- Check patients
SELECT first_name, last_name, gender, city, state FROM patient;

-- Check identifiers
SELECT p.first_name, p.last_name, pi.identifier_type, pi.identifier_value
FROM patient p
JOIN patient_identifier pi ON p.id = pi.patient_id
ORDER BY p.last_name, pi.identifier_type;
```

## Troubleshooting

### Issue: Init script didn't run

**Solution:**

```bash
# Remove volume and start fresh
docker-compose down -v
docker-compose up -d
```

### Issue: No data in database

**Check:**

1. Volume mount is correct in `docker-compose.yml`
2. Init script file exists: `init-scripts/01-init-db.sql`
3. Init script has correct permissions (should be readable)

**Verify:**

```bash
# Check if volume is mounted
docker inspect fhir-mysql | grep -A 10 Mounts

# Check for init script messages
docker-compose logs mysql | grep "01-init-db.sql"
```

### Issue: Duplicate key errors

The init script uses `ON DUPLICATE KEY UPDATE` to handle re-runs gracefully. If you see duplicate key errors, it means the script is trying to insert data that already exists (expected behavior on restart).

### Issue: Application drops all data on startup

**Problem:** Hibernate `ddl-auto` is set to `create-drop`  
**Solution:** Change to `update` in `application.yaml`

## Testing the Setup

### 1. Start fresh

```bash
docker-compose down -v
docker-compose up -d
sleep 30  # Wait for MySQL initialization
```

### 2. Verify database

```bash
docker exec -it fhir-mysql mysql -uroot -proot -e "USE fhir_db; SELECT COUNT(*) AS users FROM users; SELECT COUNT(*) AS patients FROM patient; SELECT COUNT(*) AS identifiers FROM patient_identifier;"
```

**Expected:**

- users: 3
- patients: 8
- identifiers: 22

### 3. Start application

```bash
mvn spring-boot:run
```

### 4. Test with Postman

- Login with `admin` / `password`
- Query for existing patients
- Verify 8 patients are returned

## File Structure

```
api/
├── docker-compose.yml              # MySQL orchestration with volume mount
├── init-scripts/
│   └── 01-init-db.sql             # Database initialization script
└── src/main/resources/
    └── application.yaml            # Hibernate configured for 'update'
```

## Summary

✅ **Automatic**: Data loads when `docker-compose up` is first run  
✅ **Persistent**: Data survives container restarts  
✅ **Safe**: Uses `ON DUPLICATE KEY UPDATE` to prevent errors  
✅ **Complete**: 3 users, 8 patients, 22 identifiers ready to use  
✅ **Compatible**: Works with Hibernate `update` mode

No manual SQL execution needed - just run `docker-compose up -d` and you're ready to go! 🚀
