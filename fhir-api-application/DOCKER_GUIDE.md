# Docker Guide for FHIR GraphQL API

## Prerequisites

- Docker Desktop installed
- Docker Compose installed

## Starting MySQL with Docker

### Option 1: Using Docker Compose (Recommended)

```bash
# Start MySQL container
docker-compose up -d

# Check if MySQL is running
docker-compose ps

# View logs
docker-compose logs -f mysql
```

### Option 2: Using Docker CLI

```bash
# Run MySQL container
docker run -d \
  --name fhir-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=fhir_db \
  -p 3306:3306 \
  mysql:8.0

# Check container status
docker ps

# View logs
docker logs -f fhir-mysql
```

## Verify MySQL is Running

```bash
# Check container health
docker-compose ps

# Connect to MySQL
docker exec -it fhir-mysql mysql -uroot -proot

# Inside MySQL shell
USE fhir_db;
SHOW TABLES;
```

## Running the Spring Boot Application

### Option 1: Run Locally (Maven)

```bash
# Make sure MySQL is running first
docker-compose up -d

# Run the application
mvn clean spring-boot:run
```

### Option 2: Build and Run with Docker

```bash
# Build the Docker image
docker build -t fhir-api:latest .

# Run the application container
docker run -d \
  --name fhir-api \
  -p 9090:9090 \
  --network fhir-network \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/fhir_db?createDatabaseIfNotExist=true \
  fhir-api:latest
```

## Useful Docker Commands

### View Container Logs

```bash
# MySQL logs
docker-compose logs -f mysql

# Application logs (if running in Docker)
docker logs -f fhir-api
```

### Stop Containers

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (will delete data)
docker-compose down -v
```

### Restart MySQL

```bash
docker-compose restart mysql
```

### Access MySQL Shell

```bash
# Using docker-compose
docker-compose exec mysql mysql -uroot -proot

# Using docker
docker exec -it fhir-mysql mysql -uroot -proot
```

### Check MySQL Data

```sql
-- Inside MySQL shell
USE fhir_db;

-- View users
SELECT id, username, email, role FROM users;

-- View patients
SELECT id, first_name, last_name, gender FROM patient;

-- View patient identifiers
SELECT pi.identifier_type, pi.identifier_value, p.first_name, p.last_name
FROM patient_identifier pi
JOIN patient p ON pi.patient_id = p.id;
```

## Troubleshooting

### MySQL Container Won't Start

```bash
# Check if port 3306 is already in use
netstat -ano | findstr :3306

# Remove existing container
docker rm -f fhir-mysql

# Start fresh
docker-compose up -d
```

### Application Can't Connect to MySQL

1. Make sure MySQL is running: `docker-compose ps`
2. Check MySQL logs: `docker-compose logs mysql`
3. Verify connection string in `application.yaml`
4. Wait for MySQL health check to pass (~10-20 seconds)

### Reset Everything

```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Remove application container if exists
docker rm -f fhir-api

# Start fresh
docker-compose up -d

# Wait for MySQL to be ready
docker-compose logs -f mysql
# Look for: "ready for connections"

# Run application
mvn clean spring-boot:run
```

## Environment Variables

You can customize MySQL settings in `docker-compose.yml`:

```yaml
environment:
  MYSQL_ROOT_PASSWORD: root # Root password
  MYSQL_DATABASE: fhir_db # Database name
  MYSQL_USER: fhir_user # Additional user
  MYSQL_PASSWORD: fhir_pass # User password
```

## Data Persistence

MySQL data is persisted in a Docker volume named `mysql-data`. To completely reset:

```bash
docker-compose down -v
```

This will delete all data and start fresh on next startup.
