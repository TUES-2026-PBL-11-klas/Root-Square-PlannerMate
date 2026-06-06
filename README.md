# friend-service

Standalone Spring Boot microservice for the friend system.
Runs independently of the IAM service on port **8081**.

---

## Architecture

```
  Client
    │  JWT in Authorization header
    ▼
friend-service (:8081)
    │  Validates JWT locally (shared secret)
    │  Forwards JWT for user lookups
    ▼
iam-service (:8080)           friend_system DB (PostgreSQL)
  /api/users/me               friends table
  /api/internal/users/{id}
```

### Key design decisions

**JWT validation** is done locally — the friend service shares the same
secret as the IAM service and verifies the token itself. No round-trip
to IAM on every request.

**User data** (email, existence) is fetched from the IAM service via HTTP
by forwarding the caller's own JWT. This means no duplicate user storage
and no service-to-service secrets needed.

**The `friends` table stores only UUIDs** — not foreign keys — because
the `users` table lives in a separate database.

---

## Setup

### 1. Create the database
```sql
CREATE DATABASE friend_system;
```
Hibernate creates the `friends` table automatically on first startup
(`ddl-auto=update`).

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/friend_system
spring.datasource.password=your_password

# Must match the IAM service jwt.secret exactly
jwt.secret=your-very-secure-and-very-long-secret-key-here-12345

iam.service.base-url=http://localhost:8080
```

### 3. Add InternalUserController to the IAM service
Copy `IAM_ADDITION_InternalUserController.java` into:
```
iam-service/src/main/java/com/enterprise/iam_service/controller/
```
This gives the friend service a safe endpoint to resolve user emails.

### 4. Run both services
```bash
# Terminal 1
cd iam-service && ./mvnw spring-boot:run

# Terminal 2
cd friend-service && ./mvnw spring-boot:run
```

---

## Endpoints

All endpoints require `Authorization: Bearer <jwt>`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/friends` | Your accepted friends |
| `GET` | `/api/friends/requests/incoming` | Pending requests you received |
| `GET` | `/api/friends/requests/outgoing` | Pending requests you sent |
| `POST` | `/api/friends/requests` | Send a friend request |
| `POST` | `/api/friends/requests/{id}/accept` | Accept (receiver only) |
| `POST` | `/api/friends/requests/{id}/reject` | Reject (receiver only) |
| `DELETE` | `/api/friends/requests/{id}` | Cancel sent request (requester only) |
| `DELETE` | `/api/friends/{id}` | Remove accepted friendship (either party) |

### Send a request
```http
POST /api/friends/requests
Authorization: Bearer <jwt>
Content-Type: application/json

{ "receiverId": "550e8400-e29b-41d4-a716-446655440000" }
```

### Response shape
```json
{
  "id": 1,
  "requesterId": "aaa-...",
  "requesterEmail": "alice@example.com",
  "receiverId": "550e-...",
  "receiverEmail": "bob@example.com",
  "status": "PENDING",
  "createdAt": "2026-06-05T10:00:00"
}
```

---

## Error responses

| Scenario | HTTP |
|----------|------|
| User not found | 404 |
| Self-friending | 400 |
| Duplicate request | 409 |
| Wrong user for action | 403 |
| Request already resolved | 409 |
| IAM service down | 500 |

---

## File structure

```
friend-service/
├── pom.xml
├── src/main/resources/application.properties
└── src/main/java/com/enterprise/friend_service/
    ├── FriendServiceApplication.java
    ├── config/
    │   ├── SecurityConfig.java
    │   └── RestClientConfig.java
    ├── security/
    │   ├── JwtUtils.java
    │   └── JwtAuthenticationFilter.java
    ├── client/
    │   ├── IamClient.java
    │   └── IamUserResponse.java
    ├── model/Friend.java
    ├── repository/FriendRepository.java
    ├── dto/
    │   ├── SendFriendRequest.java
    │   ├── FriendResponse.java
    │   └── FriendSummary.java
    ├── service/FriendService.java
    ├── controller/FriendController.java
    └── exception/GlobalExceptionHandler.java

IAM service addition:
└── IAM_ADDITION_InternalUserController.java  ← copy to iam-service
```
