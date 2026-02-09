# ADR 001: Query Bus for Cross-Module Communication

## Status

Accepted

## Context

In our microservice architecture with bounded contexts (modules like `auth` and `user` within services), we faced a challenge: how should modules communicate when one needs read-only data from another?

### The Problem

The `auth` module needs user authentication data (userId, passwordHash, status) for login operations. However, directly importing from the `user` module's domain/application layers violates our bounded context boundaries:

```java
// ❌ This violates bounded context isolation
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.application.ports.out.UserRepository;
```

### Constraints

1. **Bounded Context Isolation**: Modules must not import from each other's domain/application layers
2. **Read-Only Access**: Auth only needs to read user data, not modify it
3. **Synchronous Requirements**: Login operations require immediate responses
4. **Type Safety**: We want compile-time guarantees about query contracts
5. **Testability**: Cross-module communication should be easily mockable

### Alternatives Considered

1. **Direct Repository Import**
   - ❌ Violates bounded context boundaries
   - ❌ Creates tight coupling between modules
   - ❌ Makes modules harder to evolve independently

2. **Domain Events (Async)**
   - ❌ Asynchronous, doesn't work for synchronous reads during login
   - ❌ Requires eventual consistency for read models
   - ✅ Good for notifications and state changes

3. **REST API Calls Between Modules**
   - ❌ Too heavyweight for in-process communication
   - ❌ Adds network overhead and latency
   - ❌ Requires service discovery within the same service

4. **Query Bus Pattern**
   - ✅ Maintains bounded context isolation
   - ✅ Synchronous read operations
   - ✅ Type-safe contracts
   - ✅ Easy to test with mocks
   - ✅ Decouples modules through shared contracts

## Decision

We will implement a **Query Bus** pattern for synchronous cross-module data queries.

### Architecture

```
common/
├── queries/
│   ├── Query.java              # Marker interface
│   ├── QueryBus.java            # Execute queries
│   ├── QueryHandler.java        # Handler interface
│   ├── QueryResult.java         # Result wrapper
│   └── exceptions/              # Query-specific exceptions

identity-service/
├── auth/ (bounded context)
│   ├── application/
│   │   └── services/LoginService.java
│   │       └── queryBus.execute(GetUserAuthDataQuery) → QueryResult<UserAuthDataDTO>
│   ├── domain/
│   │   └── AuthUser.java       # Auth's view of user data
│   └── infrastructure/
│       └── persistence/AuthUserRepositoryAdapter.java
│
├── user/ (bounded context)
│   ├── application/
│   │   └── queryhandlers/GetUserAuthDataQueryHandler.java
│   └── infrastructure/
│       └── persistence/
│           └── JpaUserRepository.java  # Shared infrastructure
│
└── shared/
    ├── contracts/queries/       # Shared query contracts
    │   ├── GetUserAuthDataQuery.java
    │   └── UserAuthDataDTO.java
    └── infrastructure/
        └── SpringQueryBus.java  # Implementation
```

### Key Design Decisions

#### 1. QueryResult for Explicit Error Handling

Queries return `QueryResult<T>` instead of nullable values:

```java
public sealed interface QueryResult<T> {
  record Success<T>(T value) implements QueryResult<T> {}
  record NotFound<T>() implements QueryResult<T> {}
  record Error<T>(String message, Throwable cause) implements QueryResult<T> {}
}
```

**Rationale**: Makes success/not-found/error cases explicit and forces consumers to handle all scenarios.

#### 2. Each Module Defines Its Own Domain Model

```java
// auth/domain/AuthUser.java - Auth's projection of user data
public record AuthUser(UserId userId, PasswordHash passwordHash, UserStatus status) {
  public static AuthUser fromDTO(UserAuthDataDTO dto) { ... }
}

// user/domain/User.java - User's full aggregate
public class User extends AggregateRoot { ... }
```

**Rationale**: Each module owns its domain model and evolves independently. DTOs are used only for contracts.

#### 3. Infrastructure Layer Can Share Persistence

```java
// Both modules can use JpaUserRepository in their adapters
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> { ... }
```

**Rationale**: Avoids duplicate persistence entities for the same table while maintaining domain layer separation.

#### 4. Contract Versioning

Queries include version metadata:

```java
public record GetUserAuthDataQuery(UUID userId) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserAuthData.v1";
  }
}
```

**Rationale**: Enables future contract evolution with backward compatibility.

#### 5. Startup Validation

SpringQueryBus validates handler registration at startup:

```java
// Fails fast if duplicate handlers exist for same query
if (handlerList.size() > 1) {
  throw new IllegalStateException("Multiple handlers registered for query...");
}
```

**Rationale**: Catches configuration errors early rather than at runtime.

## Implementation Notes

### Usage Example

```java
@Service
public class LoginService implements LoginUseCase {
  private final QueryBus queryBus;

  public LoginResult login(LoginCommand cmd) {
    // Query user data from user module
    UserAuthDataDTO userDTO = queryBus
        .execute(new GetUserByUsernameOrEmailQuery(cmd.usernameOrEmail()))
        .orElseThrow(result -> new InvalidCredentialsException());

    // Convert to auth's domain model
    AuthUser user = AuthUser.fromDTO(userDTO);

    // Continue with authentication logic...
  }
}
```

### Testing

Queries are easily mockable:

```java
@Test
void shouldLogin_WhenCredentialsValid() {
  // Mock the query bus
  when(queryBus.execute(any(GetUserByUsernameOrEmailQuery.class)))
      .thenReturn(new QueryResult.Success<>(userDTO));

  // Test login flow
  LoginResult result = loginService.login(command);

  assertThat(result.accessToken()).isNotNull();
}
```

### Performance Considerations

- **In-Process**: Query bus is synchronous and in-process (no network calls)
- **Logging**: Automatic query execution logging with timing metrics
- **Caching**: Can be added at the handler level if needed

## Consequences

### Positive

✅ **Maintains Bounded Context Isolation**: Modules communicate through contracts, not direct imports
✅ **Type Safety**: Compile-time verification of query contracts
✅ **Testability**: Easy to mock query bus in unit tests
✅ **Observability**: Centralized logging and metrics for all queries
✅ **Fail-Fast**: Startup validation catches configuration errors early
✅ **Explicit Error Handling**: QueryResult forces handling of not-found and error cases
✅ **Evolvability**: Modules can evolve independently; contracts can be versioned

### Negative

❌ **Indirection**: One extra layer of abstraction vs direct repository calls
❌ **Learning Curve**: Team needs to understand when to use queries vs events vs direct calls
❌ **DTO Mapping**: Requires mapping between domain models and DTOs

### Neutral

⚖️ **Synchronous Only**: Query bus is for sync reads; use domain events for async communication
⚖️ **Single Handler Per Query**: Enforced by validation; requires unique query types

## Related Decisions

- **ADR 002** (future): Domain Events for Async Communication
- **ADR 003** (future): Shared Infrastructure Layer Guidelines

## References

- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Bounded Context (DDD)](https://martinfowler.com/bliki/BoundedContext.html)
- Project: [docs/ARCHITECTURE.md](../ARCHITECTURE.md)
- Implementation: [common/src/main/java/com/tickon/common/queries/](../../common/src/main/java/com/tickon/common/queries/)
