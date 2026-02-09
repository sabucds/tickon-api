# Architecture Guidelines

## Hexagonal Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ Controllers │  │ Persistence │  │ Security Adapters   │ │
│  │ (Web)       │  │ Adapters    │  │ (JWT, Argon2)       │ │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘ │
└─────────┼────────────────┼────────────────────┼─────────────┘
          │ implements     │ implements         │ implements
┌─────────┼────────────────┼────────────────────┼─────────────┐
│         ▼                ▼                    ▼             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ Input Ports │  │Output Ports │  │ Output Ports        │ │
│  │ (Use Cases) │  │(Repositories│  │ (PasswordHasher,    │ │
│  │             │  │             │  │  TokenProvider)     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
│                      APPLICATION                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Services (Use Case implementations)                  │  │
│  │ DTOs (Commands, Results)                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────┘
                              │ uses
┌─────────────────────────────┼───────────────────────────────┐
│                      DOMAIN │                               │
│  ┌──────────────┐  ┌────────┴───┐  ┌─────────────────────┐ │
│  │ Aggregates   │  │   Value    │  │ Domain Exceptions   │ │
│  │ (User,       │  │  Objects   │  │ Policies            │ │
│  │  Session)    │  │            │  │ Domain Events       │ │
│  └──────────────┘  └────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.tickon.{service}/
├── {subdomain}/                    # e.g., user, auth, event
│   ├── domain/
│   │   ├── {Aggregate}.java        # e.g., User.java, Session.java
│   │   ├── valueobjects/
│   │   │   └── {ValueObject}.java  # e.g., Email.java, UserId.java
│   │   ├── exceptions/
│   │   │   └── {Exception}.java    # e.g., DuplicateEmailException.java
│   │   ├── events/
│   │   │   └── {Event}.java        # e.g., UserCreatedEvent.java
│   │   └── policies/
│   │       └── {Policy}.java       # e.g., PasswordStrengthPolicy.java
│   │
│   ├── application/
│   │   ├── ports/
│   │   │   ├── in/
│   │   │   │   └── {UseCase}.java  # e.g., RegisterUserUseCase.java
│   │   │   └── out/
│   │   │       └── {Port}.java     # e.g., UserRepository.java
│   │   ├── services/
│   │   │   └── {Service}.java      # e.g., RegisterUserService.java
│   │   └── dto/
│   │       ├── {Command}.java      # e.g., RegisterUserCommand.java
│   │       └── {Result}.java       # e.g., UserResult.java
│   │
│   └── infrastructure/
│       ├── web/
│       │   ├── {Controller}.java
│       │   ├── dto/
│       │   │   ├── {Request}.java
│       │   │   └── {Response}.java
│       │   └── mappers/
│       │       └── {Mapper}.java
│       ├── persistence/
│       │   ├── {Adapter}.java      # e.g., UserRepositoryAdapter.java
│       │   ├── Jpa{Repository}.java
│       │   ├── entities/
│       │   │   └── {Entity}.java
│       │   └── mappers/
│       │       └── {Mapper}.java
│       └── security/
│           └── {Adapter}.java      # e.g., Argon2PasswordHasher.java
│
├── config/                         # Spring configuration
├── shared/
│   ├── domain/
│   │   ├── AggregateRoot.java      # Base class with domain events
│   │   └── DomainEvent.java        # Domain event interface
│   └── errors/
│       ├── DomainException.java
│       ├── ErrorCode.java
│       └── GlobalExceptionHandler.java
```

## Domain Layer Rules

### Aggregates

```java
public class User extends AggregateRoot {
    private final UserId id;           // Immutable identity
    private Email email;               // Mutable state via methods

    // Private constructor - enforces factory usage
    private User(UserId id, Email email, ...) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
    }

    // Factory for NEW entities - registers domain events
    public static User create(UserId id, Email email, ...) {
        User user = new User(id, email, ...);
        user.registerEvent(new UserCreatedEvent(id, email));
        return user;
    }

    // Factory for RESTORING from persistence - NO events
    public static User restore(UserId id, Email email, ...) {
        return new User(id, email, ...);
    }

    // Behavior methods - not setters
    public void changeEmail(Email newEmail) {
        Email oldEmail = this.email;
        this.email = Objects.requireNonNull(newEmail, "newEmail");
        registerEvent(new UserEmailChangedEvent(id, oldEmail, newEmail));
    }

    // Accessors without "get" prefix
    public UserId id() { return id; }
    public Email email() { return email; }
}
```

### AggregateRoot Base Class

```java
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> domainEvents() {
        return new ArrayList<>(domainEvents);  // Defensive copy for safe event publishing
    }

    public void clearEvents() {
        domainEvents.clear();
    }
}
```

### Domain Events

```java
public interface DomainEvent {
    Instant occurredOn();
}

public record UserCreatedEvent(
    UserId userId,
    Email email,
    Instant occurredOn
) implements DomainEvent {
    public UserCreatedEvent(UserId userId, Email email) {
        this(userId, email, Instant.now());
    }
}

public record UserEmailChangedEvent(
    UserId userId,
    Email oldEmail,
    Email newEmail,
    Instant occurredOn
) implements DomainEvent {
    public UserEmailChangedEvent(UserId userId, Email oldEmail, Email newEmail) {
        this(userId, oldEmail, newEmail, Instant.now());
    }
}
```

### Value Objects

```java
public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException(value);
        }
    }

    public static Email from(String value) {
        return new Email(value);
    }
}
```

### Domain Exceptions

```java
public class DuplicateEmailException extends IdentityDomainException {
    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL, "Email already in use: " + email);
    }
}
```

## Application Layer Rules

### Use Case Interface (Input Port)

```java
public interface RegisterUserUseCase {
    UserResult register(RegisterUserCommand command);
}
```

### Repository Interface (Output Port)

```java
public interface UserRepository {
    Optional<User> findById(UserId id);
    boolean existsByEmail(Email email);
    void save(User user);
    void delete(UserId id);
}
```

### Event Publisher Interface (Output Port)

```java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
}
```

### Service Implementation

```java
@Service
public class RegisterUserService implements RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordStrengthPolicy passwordPolicy;
    private final DomainEventPublisher eventPublisher;

    // Constructor injection only
    public RegisterUserService(UserRepository userRepository, ...) { ... }

    @Override
    @Transactional
    public UserResult register(RegisterUserCommand cmd) {
        // 1. Validation
        if (userRepository.existsByEmail(cmd.email()))
            throw new DuplicateEmailException(cmd.email().value());

        // 2. Policy check
        passwordPolicy.validate(cmd.rawPassword());

        // 3. Domain operation (events registered inside)
        PasswordHash hash = passwordHasher.hash(cmd.rawPassword());
        User user = User.create(UserId.generate(), cmd.email(), ...);

        // 4. Persistence
        userRepository.save(user);

        // 5. Publish domain events
        eventPublisher.publishAll(user.domainEvents());
        user.clearEvents();

        // 6. Return result
        return UserResult.from(user);
    }
}
```

## Infrastructure Layer Rules

### Repository Adapter

```java
@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository jpa;
    private final UserPersistenceMapper mapper;

    @Override
    public void save(User user) {
        jpa.save(mapper.toEntity(user));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }
}
```

### Event Publisher Adapter

```java
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final RegisterUserUseCase registerUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        var result = registerUser.register(UserMapper.toCommand(request));
        return UserMapper.toResponse(result);
    }
}
```

## Bounded Context Communication

Modules within a service (e.g., `auth` and `user`) are separate bounded contexts. Separation applies to **domain and application layers**, while **infrastructure can be shared**.

### Layer-by-Layer Rules

| Layer | Can Import From Other Module? | Reason |
|-------|------------------------------|--------|
| **Domain** | ❌ No | Each context owns its domain model |
| **Application** | ❌ No | Ports/services are context-specific |
| **Infrastructure** | ✅ Yes | Integration point, avoids duplication |

### Example: Auth Accessing User Data

```
user module                              auth module
───────────                              ───────────
User (domain)                            AuthUser (domain)        ← Different models
UserRepository (port)                    AuthUserRepository (port) ← Different ports
UserRepositoryAdapter                    AuthUserRepositoryAdapter
        │                                        │
        └──────► JpaUserRepository ◄─────────────┘               ← Shared JPA repo
                        │
                   UserEntity                                     ← Single entity
                        │
                   [users table]
```

### Auth Module Implementation

```java
// auth/domain/AuthUser.java - Auth's own domain model (read-only projection)
public class AuthUser {
    private final UserId id;
    private final PasswordHash passwordHash;
    private final UserStatus status;
    // Only fields auth cares about - no email, username, etc.
}

// auth/application/ports/out/AuthUserRepository.java - Auth's own port
public interface AuthUserRepository {
    Optional<AuthUser> findById(UserId id);
    Optional<AuthUser> findByUsernameOrEmail(String usernameOrEmail);
}

// auth/infrastructure/persistence/AuthUserRepositoryAdapter.java
@Repository
public class AuthUserRepositoryAdapter implements AuthUserRepository {
    private final JpaUserRepository jpaRepository;  // ✅ Shared from user's infrastructure

    @Override
    public Optional<AuthUser> findByUsernameOrEmail(String usernameOrEmail) {
        return jpaRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .map(this::toAuthUser);  // Maps UserEntity → AuthUser
    }

    private AuthUser toAuthUser(UserEntity entity) {
        return new AuthUser(
            new UserId(entity.id),
            new PasswordHash(entity.passwordHash),
            UserStatus.valueOf(entity.status)
        );
    }
}
```

### Why Share Infrastructure?

1. **Single source of truth** - One JPA entity per table
2. **Consistent behavior** - Soft-delete filters, lifecycle callbacks applied once
3. **No Hibernate conflicts** - Avoids L2 cache issues with multiple entities
4. **Easier maintenance** - Schema changes in one place

### What NOT to Share

- Domain entities (`User` vs `AuthUser`)
- Application ports (`UserRepository` vs `AuthUserRepository`)
- Application services
- Domain events (unless explicitly designed for cross-context communication)

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Aggregate | `{Name}` | `User`, `Session`, `Event` |
| Value Object | `{Name}` | `Email`, `UserId`, `Username` |
| Domain Event | `{Entity}{Action}Event` | `UserCreatedEvent`, `SessionRevokedEvent` |
| Domain Exception | `{Problem}Exception` | `DuplicateEmailException` |
| Use Case | `{Action}{Entity}UseCase` | `RegisterUserUseCase` |
| Service | `{Action}{Entity}Service` | `RegisterUserService` |
| Command DTO | `{Action}{Entity}Command` | `RegisterUserCommand` |
| Result DTO | `{Entity}Result` | `UserResult` |
| Request DTO | `{Action}{Entity}Request` | `RegisterUserRequest` |
| Response DTO | `{Entity}Response` | `UserResponse` |
| Repository Port | `{Entity}Repository` | `UserRepository` |
| Repository Adapter | `{Entity}RepositoryAdapter` | `UserRepositoryAdapter` |
| JPA Repository | `Jpa{Entity}Repository` | `JpaUserRepository` |
| Persistence Mapper | `{Entity}PersistenceMapper` | `UserPersistenceMapper` |
