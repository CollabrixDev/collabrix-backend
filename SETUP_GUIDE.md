# SyncSpace Backend - Phase 1 Setup Guide

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 13+
- Git

### Local Development Setup

#### 1. Clone the Repository
```bash
cd collabrix-backend
```

#### 2. Create PostgreSQL Database
```sql
CREATE DATABASE syncspace;
CREATE USER syncspace_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE syncspace TO syncspace_user;
```

#### 3. Configure Environment Variables
Create a `.env` file in the project root:
```
DB_URL=jdbc:postgresql://localhost:5432/syncspace
DB_USERNAME=syncspace_user
DB_PASSWORD=your_password
JWT_SECRET=your-super-secret-jwt-key-min-32-characters-long
```

Or set as system environment variables.

#### 4. Install Dependencies and Run
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### API Documentation
Once the application is running, access Swagger UI:
```
http://localhost:8080/api/swagger-ui/index.html
```

## Phase 1 Implementation - Authentication & Authorization

### Features Implemented

#### 1. ✅ Spring Boot Setup
- Spring Boot 3.2.0 with Java 21
- PostgreSQL integration with JPA
- Maven build configuration

#### 2. ✅ JWT Authentication
- JWT token generation and validation using JJWT
- Token stored in Authorization header with Bearer scheme
- 24-hour token expiration (configurable)

#### 3. ✅ User Entity & Database Schema
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);
```

#### 4. ✅ Authentication Endpoints

##### Signup (POST /auth/signup)
```json
Request:
{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePassword123"
}

Response:
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "role": "MEMBER",
        "createdAt": "2024-01-15T10:30:00",
        "isActive": true
    },
    "message": "User registered successfully"
}
```

##### Login (POST /auth/login)
```json
Request:
{
    "email": "john@example.com",
    "password": "securePassword123"
}

Response:
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "role": "MEMBER",
        "createdAt": "2024-01-15T10:30:00",
        "isActive": true
    },
    "message": "Login successful"
}
```

##### Get Current User (GET /auth/me)
**Headers**: `Authorization: Bearer {token}`

```json
Response:
{
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "MEMBER",
    "createdAt": "2024-01-15T10:30:00",
    "isActive": true
}
```

##### Get User by ID (GET /auth/users/{userId})
**Headers**: `Authorization: Bearer {token}`

#### 5. ✅ Security Features
- **Password Hashing**: BCrypt with Spring Security
- **JWT Validation**: Custom filter validates tokens on each request
- **CORS Configuration**: Allows frontend requests from localhost:3000 and localhost:5173
- **Exception Handling**: Global exception handler with proper HTTP status codes
- **Input Validation**: Request validation with custom error messages

#### 6. ✅ Exception Handling
The application handles the following exceptions:
- `ResourceNotFoundException` (404)
- `DuplicateResourceException` (409)
- `BadCredentialsException` (401)
- `MethodArgumentNotValidException` (400)
- Generic exceptions (500)

### Project Structure

```
src/main/java/com/syncspace/
├── config/
│   ├── SecurityConfig.java       # Spring Security configuration
│   ├── CorsConfig.java            # CORS configuration
│   └── SwaggerConfig.java         # OpenAPI/Swagger configuration
├── controller/
│   └── AuthController.java        # Authentication endpoints
├── service/
│   └── AuthService.java           # Authentication business logic
├── entity/
│   └── User.java                  # User JPA entity
├── repository/
│   └── UserRepository.java        # User database access
├── security/
│   ├── JwtUtil.java              # JWT utilities
│   ├── CustomUserDetailsService.java  # User details loading
│   └── JwtAuthenticationFilter.java  # JWT validation filter
├── dto/
│   ├── SignupRequest.java        # Signup payload
│   ├── LoginRequest.java         # Login payload
│   ├── UserDTO.java              # User response DTO
│   └── AuthResponse.java         # Authentication response
├── exception/
│   ├── GlobalExceptionHandler.java  # Exception handling
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── ErrorResponse.java        # Error response format
└── SyncSpaceApplication.java     # Main application class
```

### Configuration Files

#### application.yml
- Database connection settings
- JWT configuration (secret, expiration)
- Logging levels
- Swagger/OpenAPI settings
- CORS settings

#### pom.xml
Dependencies:
- Spring Boot Web Starter
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- JWT (JJWT)
- Validation (Jakarta)
- Lombok
- Swagger/SpringDoc OpenAPI

### Testing the APIs

#### Using cURL

```bash
# Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# Get Current User (replace TOKEN with actual JWT)
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer TOKEN"
```

#### Using Swagger UI
1. Navigate to `http://localhost:8080/api/swagger-ui/index.html`
2. Click on the endpoint you want to test
3. Click "Try it out"
4. Enter the request body
5. Click "Execute"

### Next Steps (Phase 2)

Phase 2 will focus on:
- ✅ Workspace APIs (Create, List, Join)
- ✅ Workspace Member Management
- ✅ Task APIs (CRUD operations)
- ✅ Input validation and error handling

### Troubleshooting

#### Database Connection Issues
- Ensure PostgreSQL is running: `pg_isready`
- Check database credentials in `application.yml`
- Verify database exists: `psql -l`

#### Port Already in Use
- Change port in `application.yml`: `server.port: 8081`
- Or kill existing process: `lsof -i :8080`

#### JWT Token Errors
- Ensure `JWT_SECRET` environment variable is set
- Token might be expired (24 hours default)
- Verify Bearer token format in header

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DB_URL | jdbc:postgresql://localhost:5432/syncspace | Database connection URL |
| DB_USERNAME | postgres | Database user |
| DB_PASSWORD | password | Database password |
| JWT_SECRET | your-secret-key... | JWT signing secret |

### Security Notes

- Change `JWT_SECRET` in production to a secure random value (min 32 chars)
- Use HTTPS in production
- Set `CORS allowed origins` to specific domains, not "*"
- Use environment variables for sensitive data
- Enable CSRF protection in production if serving HTML

### Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Swagger/OpenAPI Documentation](https://swagger.io/tools/swagger-ui/)
