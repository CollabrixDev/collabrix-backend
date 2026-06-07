# Phase 1 Implementation - Developer Quick Reference

## Code Quality Standards Applied

### ✅ No Singletons
Every class uses constructor injection via `@RequiredArgsConstructor`:
```java
@Service
@RequiredArgsConstructor  // Constructor-based DI
public class AuthServiceImpl implements IAuthService {
    private final IUserRepository userRepository;      // Final fields
    private final PasswordEncoder passwordEncoder;
    private final IUserFactory userFactory;
}
```

### ✅ Interface-Based Design
Every service/component has interface:
- `IAuthService` → `AuthServiceImpl`
- `IUserFactory` → `UserFactoryImpl`
- `IUserRepository` → `UserRepository`
- `IUserDetailsService` → `CustomUserDetailsServiceImpl`

### ✅ Proper Exception Throwing (No Silent Fails)
```java
// DON'T - Silent fail
try {
    User user = userRepository.findByEmail(email).orElse(null);
    return user;
} catch (Exception e) {
    e.printStackTrace();  // WRONG!
}

// DO - Explicit exception
try {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return UserDTO.fromEntity(user);
} catch (ResourceNotFoundException e) {
    throw e;  // Re-throw known exception
} catch (Exception e) {
    log.error("Error fetching user: {}", email, e);
    throw new RuntimeException("Failed to retrieve user: " + e.getMessage(), e);
}
```

### ✅ Exception Escalation Strategy
```
ValidationException (400)
    ↑
Service throws InvalidInputException
    ↑
GlobalExceptionHandler catches
    ↑
Returns HTTP 400 with ErrorResponse
```

### ✅ Retry & Recovery Mechanism
```java
@Retryable(
    retryFor = {Exception.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0)  // Exponential backoff
)
public AuthResponse signup(SignupRequest request) {
    // Automatically retries 3 times: 1s, 2s, 4s delays
    User user = userFactory.createUserFromSignupRequest(request, passwordEncoder);
    return AuthResponse.builder()...build();
}
```

### ✅ Clean Code with JavaDoc
Every public class and method has JavaDoc:
```java
/**
 * Register a new user with provided credentials.
 * 
 * Process:
 * 1. Validates input is not null
 * 2. Checks if email already exists
 * 3. Encodes password with BCrypt
 * 4. Creates user entity
 * 5. Persists to database
 * 6. Generates JWT token
 * 
 * @param request SignupRequest with name, email, password
 * @return AuthResponse with JWT and UserDTO
 * @throws InvalidInputException if validation fails
 * @throws DuplicateResourceException if email exists
 * @throws RuntimeException if database operation fails
 */
@Override
@Transactional
public AuthResponse signup(SignupRequest request) { ... }
```

### ✅ Factory Pattern for Object Creation
```java
// Service uses factory, not direct instantiation
User user = userFactory.createUserFromSignupRequest(request, passwordEncoder);

// Factory handles all creation logic, validation, defaults
public class UserFactoryImpl implements IUserFactory {
    @Override
    public User createUserFromSignupRequest(SignupRequest request, PasswordEncoder encoder) {
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("SignupRequest cannot be null");
        }
        // Encode password
        String encodedPassword = encoder.encode(request.getPassword());
        // Build user
        return User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(encodedPassword)
            .role(User.UserRole.MEMBER)
            .isActive(true)
            .build();
    }
}
```

### ✅ SRP - Single Responsibility Principle
| Class | Single Responsibility |
|-------|----------------------|
| `AuthServiceImpl` | User authentication & authorization |
| `UserFactoryImpl` | User entity creation |
| `CustomUserDetailsServiceImpl` | Load user details for Spring Security |
| `JwtUtil` | JWT token generation & validation |
| `JwtAuthenticationFilter` | Validate JWT on each request |
| `GlobalExceptionHandler` | Handle and format exceptions |
| `AuthController` | REST endpoint routing |
| `SecurityConfig` | Spring Security configuration |

## File Structure & Package Organization

```
src/main/java/com/syncspace/
│
├── SyncSpaceApplication.java       # @SpringBootApplication, @EnableRetry
│
├── config/
│   ├── SecurityConfig.java         # Spring Security beans
│   ├── CorsConfig.java             # CORS beans  
│   └── SwaggerConfig.java          # OpenAPI beans
│
├── controller/
│   └── AuthController.java         # @RestController, HTTP endpoints
│
├── service/
│   ├── IAuthService.java           # Interface/Contract
│   └── AuthServiceImpl.java         # Implementation with @Service
│
├── factory/
│   ├── IUserFactory.java           # Interface/Contract
│   └── UserFactoryImpl.java         # Implementation with @Component
│
├── repository/
│   ├── IUserRepository.java        # JPA Repository interface
│   └── UserRepository.java         # Spring Data proxy
│
├── entity/
│   └── User.java                   # @Entity, JPA mappings
│
├── security/
│   ├── JwtUtil.java                # JWT utilities
│   ├── JwtAuthenticationFilter.java # Filter
│   ├── IUserDetailsService.java    # Interface
│   └── CustomUserDetailsServiceImpl.java # Implementation
│
├── dto/
│   ├── SignupRequest.java          # @Valid constraints
│   ├── LoginRequest.java           # @Valid constraints
│   ├── UserDTO.java                # Response DTO
│   └── AuthResponse.java           # Response DTO
│
└── exception/
    ├── GlobalExceptionHandler.java # @RestControllerAdvice
    ├── ErrorResponse.java          # Error response DTO
    ├── InvalidInputException.java  # Custom exception
    ├── DuplicateResourceException.java
    └── ResourceNotFoundException.java
```

## Key Annotations Used

| Annotation | Purpose | Used In |
|-----------|---------|---------|
| `@SpringBootApplication` | Main entry point | SyncSpaceApplication |
| `@EnableRetry` | Enable retry mechanism | SyncSpaceApplication |
| `@Configuration` | Bean configuration | SecurityConfig, CorsConfig, SwaggerConfig |
| `@Bean` | Create managed bean | All @Configuration classes |
| `@Service` | Service layer component | AuthServiceImpl |
| `@Component` | Generic Spring component | UserFactoryImpl |
| `@Repository` | Data access component | UserRepository |
| `@RestController` | HTTP endpoint handler | AuthController |
| `@RequestMapping` | Map HTTP requests | AuthController |
| `@PostMapping` / `@GetMapping` | HTTP method mapping | AuthController methods |
| `@Transactional` | Transaction boundary | signup() method |
| `@Retryable` | Automatic retry | signup(), login(), getCurrentUser(), getUserById() |
| `@Backoff` | Retry delay strategy | @Retryable methods |
| `@RequiredArgsConstructor` | Constructor injection | All service classes |
| `@Slf4j` | Logger injection | All service classes |
| `@RestControllerAdvice` | Global exception handler | GlobalExceptionHandler |
| `@ExceptionHandler` | Map exception to handler | GlobalExceptionHandler methods |
| `@Entity` | JPA entity mapping | User |
| `@Table` | Database table mapping | User |
| `@Id` | Primary key | User.id |
| `@GeneratedValue` | Auto-increment | User.id |
| `@Column` | Column mapping | All User fields |
| `@Enumerated` | Enum mapping | User.role |
| `@NotBlank` | Input validation | SignupRequest, LoginRequest |
| `@Email` | Email validation | SignupRequest, LoginRequest |
| `@Valid` | Enable validation | AuthController method parameters |
| `@Operation` | Swagger documentation | AuthController methods |
| `@Tag` | Swagger tag | AuthController |

## Dependency Injection Pattern

### Constructor Injection (Used Throughout)
```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final IUserRepository userRepository;      // Injected
    private final PasswordEncoder passwordEncoder;      // Injected
    private final JwtUtil jwtUtil;                     // Injected
    private final AuthenticationManager authenticationManager;  // Injected
    private final IUserFactory userFactory;             // Injected
    
    // No need to define constructor - Lombok generates it
}
```

### Spring Container Manages Everything
```
Spring Container
    ↓
Scans @Service, @Component, @Repository annotations
    ↓
Creates beans and manages lifecycle
    ↓
Injects dependencies into constructors
    ↓
No static methods, no singletons
```

## Exception Hierarchy & HTTP Status Codes

```
RuntimeException
├── InvalidInputException (400 Bad Request)
├── DuplicateResourceException (409 Conflict)
├── ResourceNotFoundException (404 Not Found)
└── Spring's BadCredentialsException (401 Unauthorized)

All mapped to ErrorResponse with:
├── status: HTTP status code
├── message: Error message
├── error: Error type
├── timestamp: When error occurred
└── path: Request path
```

## Logging Best Practices

```java
// 1. DEBUG - Component initialization
log.debug("Initializing BCryptPasswordEncoder with strength 12");
log.debug("Creating CORS configuration");

// 2. INFO - Important business events
log.info("User registered successfully with id: {}", savedUser.getId());
log.info("User logged in successfully with id: {}", user.getId());

// 3. WARN - Expected errors, recoverable issues
log.warn("User already exists with email: {}", request.getEmail());
log.warn("Expired JWT token");

// 4. ERROR - Unexpected exceptions
log.error("Error loading user details for email: {}", email, e);
log.error("Unexpected error during user registration with email: {}", request.getEmail(), e);
```

## Testing Strategy

### Unit Test Example
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @MockBean
    private IAuthService authService;  // Mock the interface
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSignup() throws Exception {
        // Given
        SignupRequest request = new SignupRequest("John", "john@example.com", "pass123");
        AuthResponse response = new AuthResponse("token", userDto, "success");
        
        // When
        when(authService.signup(request)).thenReturn(response);
        
        // Then
        mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
```

## Configuration Files

### application.yml
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/syncspace}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: ${JWT_SECRET:min-32-char-secret-key}
  expiration: 86400000  # 24 hours

logging:
  level:
    com.syncspace: DEBUG
```

### pom.xml Dependencies
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-security` - Authentication
- `spring-boot-starter-data-jpa` - Database
- `spring-retry` - Retry mechanism
- `spring-boot-starter-aop` - AOP for @Retryable
- `jjwt` - JWT tokens
- `postgresql` - Database driver
- `springdoc-openapi` - Swagger UI
- `lombok` - Boilerplate reduction

## Common Patterns

### Input Validation Pattern
```java
public void validateSignupRequest(SignupRequest request) {
    if (request == null) {
        throw new InvalidInputException("Request cannot be null");
    }
    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new InvalidInputException("Name cannot be null or empty");
    }
}
```

### Factory Usage Pattern
```java
// In service
User user = userFactory.createUserFromSignupRequest(request, passwordEncoder);
User savedUser = userRepository.save(user);
```

### Exception Escalation Pattern
```java
try {
    // Operation
    User savedUser = userRepository.save(user);
    return buildResponse(savedUser);
} catch (DuplicateResourceException | InvalidInputException e) {
    throw e;  // Known exception - re-throw
} catch (Exception e) {
    log.error("Error: {}", e.getMessage(), e);
    throw new RuntimeException("Failed to complete operation", e);  // Wrap unknown exception
}
```

### Retry Pattern
```java
@Retryable(
    retryFor = {Exception.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0)
)
public void riskyOperation() {
    // Automatically retried with exponential backoff
}
```

---

## Quick Checklist for Phase 2 Development

- [ ] Create service interface first (e.g., `IWorkspaceService`)
- [ ] Create service implementation using `@Service` + `@RequiredArgsConstructor`
- [ ] Create repository interface extending `JpaRepository`
- [ ] Create repository implementation (Spring creates proxy)
- [ ] Create factory interface and implementation for complex objects
- [ ] Use constructor injection for all dependencies (no @Autowired)
- [ ] Add @Transactional to write operations
- [ ] Add @Retryable to network/database operations
- [ ] Create DTOs with validation constraints
- [ ] Create REST controller using interface, not implementation
- [ ] Add comprehensive JavaDoc to all public methods
- [ ] Add logging at appropriate levels (DEBUG, INFO, WARN, ERROR)
- [ ] Create custom exceptions inheriting from RuntimeException
- [ ] Add exception handling in GlobalExceptionHandler
- [ ] Write unit tests mocking interfaces

---

**Phase 1 Status**: ✅ Complete - Enterprise-grade foundation established
**Ready for**: Phase 2 - Workspace Management APIs
