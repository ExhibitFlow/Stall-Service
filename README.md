# Stall Service

Spring Boot microservice for managing exhibition stalls with OAuth2/JWT authentication, Kafka event streaming, and PostgreSQL persistence.

## Table of Contents

- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Entity Model](#entity-model)
- [API Endpoints](#api-endpoints)
- [Docker Setup](#docker-setup)
- [Security & Authentication](#security--authentication)
- [Running Locally](#running-locally)
- [Testing](#testing)
- [Sample Data](#sample-data)
- [Kafka Events](#kafka-events)
- [Database Migrations](#database-migrations)
- [Error Handling](#error-handling)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)

---

## Quick Start

### Using Docker (Recommended)

```bash
# Copy environment template
cp .env.template .env

# Start all services
docker-compose up -d

# Get authentication token
./get-token.sh

# Test API
curl -H "Authorization: Bearer <token>" http://localhost:8081/api/stalls
```

**Service URLs:**
- Stall API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- Keycloak: http://localhost:8080 (admin/admin)
- PostgreSQL: localhost:5432 (stalluser/stallpass)

---

## Architecture

```
┌─────────┐           ┌──────────┐           ┌───────────────┐
│ Client  │──(1)───>  │ Keycloak │──(2)───>  │               │
│         │  Token    │ OAuth2   │  JWT      │ Stall Service │
│         │  Request  │ Server   │  Token    │  (Protected)  │
└─────────┘           └──────────┘           └───────────────┘
      │                                              │
      └────────────────(3)──────────────────────────┘
              API Request with Bearer Token
                          │
                          ▼
              ┌───────────────────────┐
              │     PostgreSQL DB     │
              │   (Stall Data)        │
              └───────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │    Kafka Broker       │
              │  (Event Streaming)    │
              └───────────────────────┘
```

### Service Network

```
┌─────────────────────────────────────────┐
│         stall-network (bridge)          │
├─────────────────────────────────────────┤
│  ┌──────────┐    ┌──────────────┐       │
│  │ postgres │    │  zookeeper   │       │
│  └────┬─────┘    └──────┬───────┘       │
│       │                 │               │
│  ┌────┴─────┐    ┌──────┴───────┐       │
│  │ keycloak │    │    kafka     │       │
│  └────┬─────┘    └──────┬───────┘       │
│       │                 │               │
│       └────────┬────────┘               │
│         ┌──────┴───────┐                │
│         │stall-service │                │
│         └──────────────┘                │
└─────────────────────────────────────────┘
     Exposed Ports:
     5432, 8080, 8081, 9092, 2181
```

---

## Features

- Stall CRUD operations with validation
- Status workflow: AVAILABLE → HELD → RESERVED
- Advanced filtering by status, size, location
- Pagination and sorting
- OAuth2/JWT authentication via Keycloak
- Kafka event publishing for state changes
- PostgreSQL with Flyway migrations
- Seeded sample data (39 stalls)
- OpenAPI/Swagger documentation
- Health checks and actuator endpoints
- Comprehensive test coverage

---

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security OAuth2 Resource Server**
- **Spring Data JPA**
- **Spring Kafka**
- **PostgreSQL 15**
- **Keycloak 23**
- **Flyway**
- **Docker & Docker Compose**
- **Kafka 3.6**
- **SpringDoc OpenAPI**
- **JUnit 5 & Mockito**

---

## Entity Model

### Stall

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated ID |
| code | String | Unique stall code (e.g., "A-001") |
| size | Enum | SMALL, MEDIUM, LARGE |
| location | String | Physical location |
| price | BigDecimal | Rental price |
| status | Enum | AVAILABLE, HELD, RESERVED |
| createdAt | Timestamp | Creation time |
| updatedAt | Timestamp | Last update time |

### Status Workflow

```
AVAILABLE ──hold──> HELD ──reserve──> RESERVED
    ▲                 │                  │
    │                 └──────release─────┘
    └───────────────release──────────────┘
```

All state transitions are idempotent.

---

## API Endpoints

### Public Endpoints (No Auth Required)

```bash
GET /actuator/health          # Health check
GET /swagger-ui.html          # Swagger UI
GET /api-docs                 # OpenAPI spec
```

### Protected Endpoints (Require JWT)

#### List & Filter Stalls
```bash
GET /api/stalls                                    # All stalls (paginated)
GET /api/stalls?page=0&size=10                     # Custom pagination
GET /api/stalls?status=AVAILABLE                   # Filter by status
GET /api/stalls?stallSize=MEDIUM                   # Filter by size
GET /api/stalls?location=Hall A                    # Filter by location
GET /api/stalls?status=AVAILABLE&stallSize=LARGE   # Combined filters
```

#### Get Individual Stalls
```bash
GET /api/stalls/1              # Get by ID
GET /api/stalls/code/A-001     # Get by code
```

#### Create Stall
```bash
POST /api/stalls
Content-Type: application/json

{
  "code": "A-001",
  "size": "MEDIUM",
  "location": "Hall A - North Wing",
  "price": 1000.00
}
```

#### Update Stall
```bash
PUT /api/stalls/1
Content-Type: application/json

{
  "location": "Hall B - Updated",
  "price": 1200.00
}
```

#### State Management
```bash
POST /api/stalls/1/hold        # Hold stall
POST /api/stalls/1/reserve     # Reserve stall
POST /api/stalls/1/release     # Release stall (make available)
```

---

## Docker Setup

### Prerequisites

- Docker 20.10+
- Docker Compose 2.0+

### Start Services

```bash
# Copy and edit environment variables
cp .env.template .env

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f stall-service
```

### Environment Variables

Edit `.env` file to customize:

```bash
# Database
POSTGRES_DB=stalldb
POSTGRES_USER=stalluser
POSTGRES_PASSWORD=stallpass

# Keycloak
KEYCLOAK_CLIENT_ID=stall-service
KEYCLOAK_CLIENT_SECRET=stall-service-secret-key-2024

# Kafka
KAFKA_TOPIC_STALL_RESERVED=stall.reserved
KAFKA_TOPIC_STALL_RELEASED=stall.released

# Application
APP_PORT=8081
```

### Health Checks

```bash
# Stall Service
curl http://localhost:8081/actuator/health

# PostgreSQL
docker exec stall-postgres pg_isready -U stalluser

# Keycloak
curl http://localhost:8080/health/ready

# Kafka
docker exec stall-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Database Access

```bash
# Connect to PostgreSQL
docker exec -it stall-postgres psql -U stalluser -d stalldb

# View stalls
SELECT code, size, location, price, status FROM stall ORDER BY code;
```

### Kafka Management

```bash
# List topics
docker exec stall-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume reserved stall events
docker exec stall-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic stall.reserved \
  --from-beginning

# Consume released stall events
docker exec stall-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic stall.released \
  --from-beginning
```

### Stop Services

```bash
# Stop all
docker-compose down

# Stop and remove volumes (reset everything)
docker-compose down -v

# Rebuild from scratch
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Security & Authentication

### Keycloak Configuration

**Realm:** `exhibitflow`  
**Client ID:** `stall-service`  
**Client Secret:** `stall-service-secret-key-2024`

### Pre-configured Users

| Username | Password | Roles | Description |
|----------|----------|-------|-------------|
| admin | admin123 | admin, user | Full access |
| manager | manager123 | user | Management access |
| viewer | viewer123 | user | Read-only |

### Get Authentication Token

#### Option 1: Helper Script (Easiest)

```bash
./get-token.sh
```

#### Option 2: cURL

```bash
# Get token
curl -X POST "http://localhost:8080/realms/exhibitflow/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=stall-service" \
  -d "client_secret=stall-service-secret-key-2024" \
  -d "username=admin" \
  -d "password=admin123"

# Extract token
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/exhibitflow/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=stall-service" \
  -d "client_secret=stall-service-secret-key-2024" \
  -d "username=admin" \
  -d "password=admin123" | jq -r '.access_token')

# Use token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/stalls
```

#### Option 3: Postman Collection

Import `Stall-Service.postman_collection.json` into Postman. Authentication requests automatically save tokens.

#### Option 4: Swagger UI

1. Open http://localhost:8081/swagger-ui.html
2. Click "Authorize"
3. Enter: `Bearer <your_token>`
4. Test endpoints

### Token Details

- **Lifetime:** 1 hour
- **Algorithm:** RS256
- **Validation:** JWK endpoint auto-fetch
- **Issuer:** `http://localhost:8080/realms/exhibitflow`

### Testing Authentication

```bash
# Without token (should fail with 401)
curl http://localhost:8081/api/stalls

# With valid token (should succeed)
TOKEN=$(./get-token.sh | grep "access_token" | cut -d'"' -f4)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/stalls
```

---

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Kafka 2.8+ (optional)
- Keycloak (optional, use Docker)

### Setup Database

```bash
# Create database
createdb stalldb

# Create user
psql -d stalldb -c "CREATE USER stalluser WITH PASSWORD 'stallpass';"
psql -d stalldb -c "GRANT ALL PRIVILEGES ON DATABASE stalldb TO stalluser;"
```

### Run Dependencies Only

```bash
# Start PostgreSQL, Kafka, Keycloak via Docker
docker-compose up -d postgres kafka zookeeper keycloak

# Run application locally
mvn spring-boot:run
```

### Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run JAR
java -jar target/stall-service-0.0.1-SNAPSHOT.jar
```

---

## Testing

### Run Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest=*ServiceTest

# Integration tests only
mvn test -Dtest=*IntegrationTest

# With coverage
mvn clean test jacoco:report
```

### Test Files

- `StallServiceTest.java` - Unit tests for business logic
- `StallControllerIntegrationTest.java` - Integration tests with test containers

---

## Sample Data

The service includes 39 pre-seeded stalls via Flyway migration `V2__seed_stalls.sql`:

### Hall A (13 stalls)
- **Small** (A-001 to A-005): $500-$550
- **Medium** (A-101 to A-105): $1000-$1050
- **Large** (A-201 to A-203): $2000-$2500

### Hall B (12 stalls)
- **Small** (B-001 to B-005): $450-$475
- **Medium** (B-101 to B-104): $950-$975
- **Large** (B-201 to B-203): $1750-$1800

### Hall C (11 stalls)
- **Small** (C-001 to C-004): $525
- **Medium** (C-101 to C-104): $1100-$1150
- **Large** (C-201 to C-203): $2200-$2300

### Outdoor Area (3 stalls)
- **Large** (OUT-001 to OUT-003): $1500-$1600

**Status Distribution:**
- AVAILABLE: 60%
- HELD: 25%
- RESERVED: 15%

---

## Kafka Events

### Stall Reserved Event
**Topic:** `stall.reserved`

```json
{
  "stallId": 1,
  "code": "A-001",
  "status": "RESERVED",
  "location": "Hall A - North Wing"
}
```

### Stall Released Event
**Topic:** `stall.released`

```json
{
  "stallId": 1,
  "code": "A-001",
  "status": "AVAILABLE",
  "location": "Hall A - North Wing"
}
```

Events are published automatically on status changes.

---

## Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:

- `V1__create_stall_table.sql` - Initial table schema
- `V2__seed_stalls.sql` - Sample data seeding

Migrations run automatically on startup.

---

## Error Handling

| Status Code | Description |
|-------------|-------------|
| 400 | Validation errors or invalid state transitions |
| 401 | Unauthorized (missing/invalid token) |
| 404 | Stall not found |
| 409 | Duplicate stall code |
| 500 | Internal server error |

Error response format:

```json
{
  "timestamp": "2025-11-22T10:30:00",
  "message": "Stall with code A-001 already exists",
  "details": "uri=/api/stalls"
}
```

---

## Troubleshooting

### 401 Unauthorized with Valid Token

**Causes:**
- Token expired (1 hour lifetime)
- Wrong client secret
- Service can't reach Keycloak JWK endpoint

**Solution:**
```bash
# Check logs
docker logs stall-service

# Verify Keycloak connectivity
docker exec stall-service wget -O- -q http://keycloak:8080/realms/exhibitflow/protocol/openid-connect/certs
```

### Can't Get Token from Keycloak

**Causes:**
- Wrong username/password
- Keycloak not running
- Realm not imported

**Solution:**
```bash
# Check Keycloak logs
docker logs stall-keycloak | grep -i exhibitflow

# Verify realm exists
curl http://localhost:8080/realms/exhibitflow/.well-known/openid-configuration
```

### Database Connection Failed

**Solution:**
```bash
# Check PostgreSQL
docker-compose ps postgres
docker exec -it stall-postgres pg_isready -U stalluser

# Restart services
docker-compose restart postgres stall-service
```

### Kafka Connection Issues

**Solution:**
```bash
# Check Kafka
docker-compose logs kafka

# Verify Kafka is ready
docker exec stall-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Complete Reset

```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

---

## Project Structure

```
src/main/java/com/exhibitflow/stall/
├── config/
│   ├── OpenApiConfig.java           # Swagger/OpenAPI configuration
│   └── SecurityConfig.java          # OAuth2/JWT security
├── controller/
│   └── StallController.java         # REST endpoints
├── dto/
│   ├── CreateStallRequest.java      # Create request DTO
│   ├── UpdateStallRequest.java      # Update request DTO
│   ├── StallResponse.java           # Response DTO
│   └── StallEventDto.java           # Kafka event DTO
├── event/
│   └── StallEventPublisher.java     # Kafka publisher
├── exception/
│   ├── GlobalExceptionHandler.java  # Global error handler
│   ├── ErrorResponse.java           # Error response format
│   └── ValidationErrorResponse.java # Validation errors
├── model/
│   ├── Stall.java                   # JPA entity
│   ├── StallSize.java               # Size enum
│   └── StallStatus.java             # Status enum
├── repository/
│   └── StallRepository.java         # JPA repository
├── service/
│   ├── StallService.java            # Business logic
│   ├── DuplicateStallCodeException.java
│   ├── InvalidStallStatusException.java
│   └── StallNotFoundException.java
└── StallServiceApplication.java     # Main application

src/main/resources/
├── application.yml                  # Application config
└── db/migration/
    ├── V1__create_stall_table.sql
    └── V2__seed_stalls.sql

Docker/Config Files:
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # Service orchestration
├── .env.template                    # Environment template
├── .dockerignore                    # Docker ignore rules
├── keycloak/
│   └── exhibitflow-realm.json       # Keycloak realm config
├── get-token.sh                     # Token helper script
├── api_test.http                    # REST Client tests
└── Stall-Service.postman_collection.json  # Postman collection
```

