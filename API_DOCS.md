# Stall Service API Documentation

Base URL: `http://localhost:8081`

## Authentication

All endpoints require JWT authentication via OAuth2/Keycloak.

**Required Header:**
```
Authorization: Bearer <access_token>
```

---

## Endpoints

### 1. List All Stalls

Retrieve a paginated list of stalls with optional filtering.

**Endpoint:** `GET /api/stalls`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Filter by status. Values: `AVAILABLE`, `HELD`, `RESERVED` |
| `stallSize` | string | No | Filter by size. Values: `SMALL`, `MEDIUM`, `LARGE` |
| `location` | string | No | Filter by location (partial match, case-insensitive) |
| `page` | integer | No | Page number (0-indexed). Default: `0` |
| `size` | integer | No | Page size. Default: `20` |
| `sort` | string | No | Sort field and direction. Default: `id,asc`. Example: `code,desc` |

**Response: 200 OK**
```json
{
  "content": [
    {
      "id": 1,
      "code": "A-001",
      "size": "MEDIUM",
      "location": "Hall A, Section 1",
      "price": 850.00,
      "status": "AVAILABLE",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "code": "A-002",
      "size": "LARGE",
      "location": "Hall A, Section 1",
      "price": 1200.00,
      "status": "HELD",
      "createdAt": "2024-01-15T10:31:00",
      "updatedAt": "2024-01-16T14:20:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 39,
  "totalPages": 2,
  "last": false,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,
  "first": true,
  "empty": false
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls",
  "status": 401,
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

---

### 2. Get Stall by ID

Retrieve a specific stall by its ID.

**Endpoint:** `GET /api/stalls/{id}`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | The unique identifier of the stall |

**Response: 200 OK**
```json
{
  "id": 5,
  "code": "A-005",
  "size": "SMALL",
  "location": "Hall A, Section 2",
  "price": 650.00,
  "status": "AVAILABLE",
  "createdAt": "2024-01-15T10:34:00",
  "updatedAt": "2024-01-15T10:34:00"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with id: 999",
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/5",
  "status": 401,
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

---

### 3. Get Stall by Code

Retrieve a specific stall by its unique code.

**Endpoint:** `GET /api/stalls/code/{code}`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `code` | string | Yes | The unique code of the stall |

**Response: 200 OK**
```json
{
  "id": 12,
  "code": "B-003",
  "size": "LARGE",
  "location": "Hall B, Section 2",
  "price": 1150.00,
  "status": "RESERVED",
  "createdAt": "2024-01-15T10:41:00",
  "updatedAt": "2024-01-16T11:20:00"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with code: X-999",
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/code/B-003",
  "status": 401,
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

---

### 4. Create Stall

Create a new stall in the system.

**Endpoint:** `POST /api/stalls`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "code": "D-001",
  "size": "MEDIUM",
  "location": "Hall D, Section 1",
  "price": 900.00
}
```

**Request Body Schema:**

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `code` | string | Yes | Max 50 chars, not blank | Unique stall code |
| `size` | string | Yes | Enum: `SMALL`, `MEDIUM`, `LARGE` | Stall size |
| `location` | string | Yes | Max 255 chars, not blank | Stall location |
| `price` | number | Yes | > 0, max 8 integer digits, 2 decimal places | Rental price |

**Response: 201 Created**
```json
{
  "id": 40,
  "code": "D-001",
  "size": "MEDIUM",
  "location": "Hall D, Section 1",
  "price": 900.00,
  "status": "AVAILABLE",
  "createdAt": "2024-01-16T15:30:00",
  "updatedAt": "2024-01-16T15:30:00"
}
```

**Response: 400 Bad Request (Validation Error)**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "code": "Code is required",
    "size": "Size is required",
    "price": "Price must be greater than 0"
  },
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

**Response: 409 Conflict**
```json
{
  "status": 409,
  "message": "Stall with code D-001 already exists",
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls",
  "status": 401,
  "timestamp": "2024-01-16T15:30:00.123456"
}
```

---

### 5. Update Stall

Update an existing stall's details.

**Endpoint:** `PUT /api/stalls/{id}`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | The unique identifier of the stall |

**Request Body:**
```json
{
  "code": "A-001-UPDATED",
  "size": "LARGE",
  "location": "Hall A, Section 1 - Prime Location",
  "price": 1500.00
}
```

**Request Body Schema:**

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `code` | string | No | Max 50 chars | Unique stall code |
| `size` | string | No | Enum: `SMALL`, `MEDIUM`, `LARGE` | Stall size |
| `location` | string | No | Max 255 chars | Stall location |
| `price` | number | No | > 0, max 8 integer digits, 2 decimal places | Rental price |

**Note:** All fields are optional. Only provided fields will be updated.

**Response: 200 OK**
```json
{
  "id": 1,
  "code": "A-001-UPDATED",
  "size": "LARGE",
  "location": "Hall A, Section 1 - Prime Location",
  "price": 1500.00,
  "status": "AVAILABLE",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T15:35:00"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with id: 999",
  "timestamp": "2024-01-16T15:35:00.123456"
}
```

**Response: 409 Conflict**
```json
{
  "status": 409,
  "message": "Stall with code A-002 already exists",
  "timestamp": "2024-01-16T15:35:00.123456"
}
```

**Response: 400 Bad Request (Validation Error)**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "price": "Price must be greater than 0",
    "code": "Code must not exceed 50 characters"
  },
  "timestamp": "2024-01-16T15:35:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/1",
  "status": 401,
  "timestamp": "2024-01-16T15:35:00.123456"
}
```

---

### 6. Hold Stall

Hold an available stall (idempotent operation).

**Endpoint:** `POST /api/stalls/{id}/hold`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | The unique identifier of the stall |

**Business Rules:**
- Only `AVAILABLE` stalls can be held
- If stall is already `HELD`, returns current state (idempotent)
- Cannot hold `RESERVED` stalls

**Response: 200 OK**
```json
{
  "id": 7,
  "code": "A-007",
  "size": "MEDIUM",
  "location": "Hall A, Section 2",
  "price": 850.00,
  "status": "HELD",
  "createdAt": "2024-01-15T10:36:00",
  "updatedAt": "2024-01-16T15:40:00"
}
```

**Response: 200 OK (Already Held - Idempotent)**
```json
{
  "id": 7,
  "code": "A-007",
  "size": "MEDIUM",
  "location": "Hall A, Section 2",
  "price": 850.00,
  "status": "HELD",
  "createdAt": "2024-01-15T10:36:00",
  "updatedAt": "2024-01-16T15:40:00"
}
```

**Response: 400 Bad Request**
```json
{
  "status": 400,
  "message": "Cannot hold stall with status: RESERVED. Only AVAILABLE stalls can be held.",
  "timestamp": "2024-01-16T15:40:00.123456"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with id: 999",
  "timestamp": "2024-01-16T15:40:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/7/hold",
  "status": 401,
  "timestamp": "2024-01-16T15:40:00.123456"
}
```

---

### 7. Release Stall

Release a held or reserved stall back to available status (idempotent operation).

**Endpoint:** `POST /api/stalls/{id}/release`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | The unique identifier of the stall |

**Business Rules:**
- Only `HELD` or `RESERVED` stalls can be released
- If stall is already `AVAILABLE`, returns current state (idempotent)
- Publishes Kafka event `stall.released` to topic

**Response: 200 OK**
```json
{
  "id": 8,
  "code": "A-008",
  "size": "SMALL",
  "location": "Hall A, Section 3",
  "price": 650.00,
  "status": "AVAILABLE",
  "createdAt": "2024-01-15T10:37:00",
  "updatedAt": "2024-01-16T15:45:00"
}
```

**Response: 200 OK (Already Available - Idempotent)**
```json
{
  "id": 8,
  "code": "A-008",
  "size": "SMALL",
  "location": "Hall A, Section 3",
  "price": 650.00,
  "status": "AVAILABLE",
  "createdAt": "2024-01-15T10:37:00",
  "updatedAt": "2024-01-16T15:45:00"
}
```

**Response: 400 Bad Request**
```json
{
  "status": 400,
  "message": "Cannot release stall with status: AVAILABLE. Only HELD or RESERVED stalls can be released.",
  "timestamp": "2024-01-16T15:45:00.123456"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with id: 999",
  "timestamp": "2024-01-16T15:45:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/8/release",
  "status": 401,
  "timestamp": "2024-01-16T15:45:00.123456"
}
```

**Kafka Event Published:**
```json
{
  "stallId": 8,
  "code": "A-008",
  "status": "AVAILABLE",
  "location": "Hall A, Section 3"
}
```
Topic: `stall.released`

---

### 8. Reserve Stall

Reserve a held stall (idempotent operation).

**Endpoint:** `POST /api/stalls/{id}/reserve`

**Headers:**
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | The unique identifier of the stall |

**Business Rules:**
- Only `HELD` stalls can be reserved
- If stall is already `RESERVED`, returns current state (idempotent)
- Cannot reserve `AVAILABLE` stalls (must be held first)
- Publishes Kafka event `stall.reserved` to topic

**Response: 200 OK**
```json
{
  "id": 10,
  "code": "A-010",
  "size": "LARGE",
  "location": "Hall A, Section 3",
  "price": 1200.00,
  "status": "RESERVED",
  "createdAt": "2024-01-15T10:39:00",
  "updatedAt": "2024-01-16T15:50:00"
}
```

**Response: 200 OK (Already Reserved - Idempotent)**
```json
{
  "id": 10,
  "code": "A-010",
  "size": "LARGE",
  "location": "Hall A, Section 3",
  "price": 1200.00,
  "status": "RESERVED",
  "createdAt": "2024-01-15T10:39:00",
  "updatedAt": "2024-01-16T15:50:00"
}
```

**Response: 400 Bad Request**
```json
{
  "status": 400,
  "message": "Cannot reserve stall with status: AVAILABLE. Only HELD stalls can be reserved.",
  "timestamp": "2024-01-16T15:50:00.123456"
}
```

**Response: 404 Not Found**
```json
{
  "status": 404,
  "message": "Stall not found with id: 999",
  "timestamp": "2024-01-16T15:50:00.123456"
}
```

**Response: 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/stalls/10/reserve",
  "status": 401,
  "timestamp": "2024-01-16T15:50:00.123456"
}
```

**Kafka Event Published:**
```json
{
  "stallId": 10,
  "code": "A-010",
  "status": "RESERVED",
  "location": "Hall A, Section 3"
}
```
Topic: `stall.reserved`

---

## Common Error Responses

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred: <error details>",
  "timestamp": "2024-01-16T15:55:00.123456"
}
```

---

## Data Types Reference

### StallSize Enum
- `SMALL`
- `MEDIUM`
- `LARGE`

### StallStatus Enum
- `AVAILABLE` - Stall is available for booking
- `HELD` - Stall is temporarily held (can be released or reserved)
- `RESERVED` - Stall is reserved (can only be released)

### Status Transition Rules

```
AVAILABLE → HELD → RESERVED → AVAILABLE
    ↑         ↓                    ↓
    └─────────┴────────────────────┘
```

**Valid Transitions:**
- `AVAILABLE` → `HELD` (via `/hold`)
- `HELD` → `RESERVED` (via `/reserve`)
- `HELD` → `AVAILABLE` (via `/release`)
- `RESERVED` → `AVAILABLE` (via `/release`)

**Invalid Transitions:**
- `AVAILABLE` → `RESERVED` (must hold first)
- `RESERVED` → `HELD` (must release first)

---

## Pagination Response Structure

All paginated endpoints return the following structure:

| Field | Type | Description |
|-------|------|-------------|
| `content` | array | Array of stall objects |
| `pageable` | object | Pagination metadata |
| `totalElements` | integer | Total number of items across all pages |
| `totalPages` | integer | Total number of pages |
| `last` | boolean | Whether this is the last page |
| `size` | integer | Page size |
| `number` | integer | Current page number (0-indexed) |
| `sort` | object | Sort information |
| `numberOfElements` | integer | Number of elements in current page |
| `first` | boolean | Whether this is the first page |
| `empty` | boolean | Whether the page is empty |

---

## Authentication

To obtain an access token, make a POST request to Keycloak:

**Endpoint:** `POST http://localhost:8080/realms/exhibitflow/protocol/openid-connect/token`

**Headers:**
```
Content-Type: application/x-www-form-urlencoded
```

**Request Body (Admin User):**
```
grant_type=password
client_id=stall-service
client_secret=stall-service-secret-key-2024
username=admin
password=admin123
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 3600,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "scope": "profile email"
}
```

**Test Users:**
- Admin: `admin` / `admin123`
- Manager: `manager` / `manager123`
- Viewer: `viewer` / `viewer123`

---

## Example Usage Scenarios

### Scenario 1: Book a Stall (Complete Flow)

1. **Search for available stalls:**
   ```
   GET /api/stalls?status=AVAILABLE&stallSize=MEDIUM
   ```

2. **Hold the stall:**
   ```
   POST /api/stalls/7/hold
   ```

3. **Reserve the stall:**
   ```
   POST /api/stalls/7/reserve
   ```

### Scenario 2: Cancel a Reservation

1. **Check stall status:**
   ```
   GET /api/stalls/10
   ```

2. **Release the stall:**
   ```
   POST /api/stalls/10/release
   ```

### Scenario 3: Update Stall Details

1. **Get current stall:**
   ```
   GET /api/stalls/code/A-005
   ```

2. **Update price and location:**
   ```
   PUT /api/stalls/5
   {
     "price": 750.00,
     "location": "Hall A, Section 2 - Updated"
   }
   ```

### Scenario 4: Filter Stalls by Location

```
GET /api/stalls?location=hall%20b&page=0&size=10&sort=price,asc
```
