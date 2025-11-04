# Exhibitflow Stall Service

A Spring Boot 3 microservice for managing exhibition stalls with JWT security, Kafka integration, and comprehensive REST API.

## Features

- **Stall Management**: Create, update, and manage exhibition stalls
- **Status Workflow**: Hold, release, and reserve stalls with idempotent operations
- **Advanced Filtering**: List and filter stalls by status, size, and location with pagination
- **JWT Security**: OAuth2 resource server with JWT authentication
- **Event Publishing**: Kafka events for stall reservation and release
- **API Documentation**: OpenAPI/Swagger UI for interactive API exploration
- **Database**: PostgreSQL with Flyway migrations
- **Comprehensive Testing**: Unit and integration tests with 100% business logic coverage

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Security OAuth2 Resource Server
- Spring Kafka
- SpringDoc OpenAPI
- JUnit 5 & Mockito
- H2 (for testing)

## Entity Model

### Stall
- `id`: Unique identifier (auto-generated)
- `code`: Unique stall code (e.g., "A-001")
- `size`: Stall size (SMALL, MEDIUM, LARGE)
- `location`: Physical location
- `price`: Rental price
- `status`: Current status (AVAILABLE, HELD, RESERVED)
- `createdAt`: Timestamp of creation
- `updatedAt`: Timestamp of last update

### Status Workflow
- **AVAILABLE** → **HELD** (via hold endpoint)
- **HELD** → **RESERVED** (via reserve endpoint)
- **HELD/RESERVED** → **AVAILABLE** (via release endpoint)

All status change operations are idempotent.

## API Endpoints

### Stall Operations

#### List Stalls (with filtering and pagination)
```
GET /api/stalls?status=AVAILABLE&size=MEDIUM&location=Hall&page=0&size=20
```

#### Get Stall by ID
```
GET /api/stalls/{id}
```

#### Create Stall
```
POST /api/stalls
Content-Type: application/json

{
  "code": "A-001",
  "size": "MEDIUM",
  "location": "Hall A",
  "price": 500.00
}
```

#### Update Stall
```
PUT /api/stalls/{id}
Content-Type: application/json

{
  "location": "Hall B",
  "price": 600.00
}
```

#### Hold Stall (Idempotent)
```
POST /api/stalls/{id}/hold
```

#### Release Stall (Idempotent)
```
POST /api/stalls/{id}/release
```

#### Reserve Stall (Idempotent)
```
POST /api/stalls/{id}/reserve
```

## Configuration

### Application Properties (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stalldb
    username: stalluser
    password: stallpass
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/exhibitflow
          jwk-set-uri: http://localhost:8080/realms/exhibitflow/protocol/openid-connect/certs

  kafka:
    bootstrap-servers: localhost:9092

kafka:
  topics:
    stall-reserved: stall.reserved
    stall-released: stall.released
```

## Running the Application

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Kafka 2.8 or higher (optional, for event publishing)
- Maven 3.6 or higher

### Setup Database
```bash
# Create database
createdb stalldb

# Create user
psql -d stalldb -c "CREATE USER stalluser WITH PASSWORD 'stallpass';"
psql -d stalldb -c "GRANT ALL PRIVILEGES ON DATABASE stalldb TO stalluser;"
```

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application will start on port 8081.

### Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

### Access API Documentation
```
http://localhost:8081/api-docs
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Unit Tests Only
```bash
mvn test -Dtest=*ServiceTest
```

### Run Integration Tests Only
```bash
mvn test -Dtest=*IntegrationTest
```

## Kafka Events

### Stall Reserved Event
Topic: `stall.reserved`

Payload:
```json
{
  "stallId": 1,
  "code": "A-001",
  "status": "RESERVED",
  "location": "Hall A"
}
```

### Stall Released Event
Topic: `stall.released`

Payload:
```json
{
  "stallId": 1,
  "code": "A-001",
  "status": "AVAILABLE",
  "location": "Hall A"
}
```

## Security

The application uses JWT-based authentication as an OAuth2 resource server. All endpoints except `/actuator/**`, `/swagger-ui/**`, and `/api-docs/**` require a valid JWT token.

### Example Request with JWT
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8081/api/stalls
```

**Note**: CSRF protection is disabled as this is a stateless REST API using JWT authentication. JWT tokens are not stored in cookies and are therefore immune to CSRF attacks.

## Database Migrations

Flyway is used for database migrations. Migration scripts are located in:
```
src/main/resources/db/migration/
```

### Current Migrations
- `V1__create_stall_table.sql`: Initial stall table creation with indexes

## Error Handling

The application provides comprehensive error handling with appropriate HTTP status codes:

- **400 Bad Request**: Validation errors or invalid state transitions
- **404 Not Found**: Stall not found
- **409 Conflict**: Duplicate stall code
- **500 Internal Server Error**: Unexpected errors

Error responses include timestamps and detailed messages.

## Project Structure

```
src/main/java/com/exhibitflow/stall/
├── config/              # Configuration classes
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/          # REST controllers
│   └── StallController.java
├── dto/                 # Data Transfer Objects
│   ├── CreateStallRequest.java
│   ├── UpdateStallRequest.java
│   ├── StallResponse.java
│   └── StallEventDto.java
├── event/               # Kafka event publishers
│   └── StallEventPublisher.java
├── exception/           # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── ErrorResponse.java
│   └── ValidationErrorResponse.java
├── model/               # JPA entities and enums
│   ├── Stall.java
│   ├── StallSize.java
│   └── StallStatus.java
├── repository/          # JPA repositories
│   └── StallRepository.java
├── service/             # Business logic
│   ├── StallService.java
│   └── *Exception.java
└── StallServiceApplication.java
```

## Contributing

This project follows standard Spring Boot conventions and best practices. Please ensure:
- All new features have corresponding tests
- Code follows existing formatting and style
- All tests pass before submitting changes

## License

This project is part of the ExhibitFlow system for Software Architecture coursework.

