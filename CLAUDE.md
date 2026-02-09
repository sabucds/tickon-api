# Tickon API - Claude Context

Ticket booking microservice platform built with Spring Boot 3, Java 21, and PostgreSQL.

## Architecture

**Hexagonal Architecture** with strict layer separation:

```
domain/           → Entities, Value Objects, Domain Exceptions, Policies
application/      → Use Cases (ports/in), Repository interfaces (ports/out), DTOs, Services
infrastructure/   → Controllers, Persistence Adapters, Security implementations
```

**Dependency Rule**: Dependencies point inward. Infrastructure → Application → Domain.

## Key Patterns

### Domain Layer
- **Aggregates**: Extend `AggregateRoot`, private constructor, `create()` for new entities (registers events), `restore()` for persistence (no events)
- **Value Objects**: Java records with validation in compact constructor
- **Domain Events**: Records implementing `DomainEvent`, named `{Entity}{Action}Event`
- **Exceptions**: Extend `DomainException` with `ErrorCode`

### Application Layer
- **Use Cases**: One interface per operation (e.g., `RegisterUserUseCase`)
- **Services**: Implement use cases, orchestrate domain objects, publish domain events
- **DTOs**: `*Command` for input, `*Result` for output
- **Output Ports**: `DomainEventPublisher` for event publishing

### Infrastructure Layer
- **Adapters**: Implement port interfaces (e.g., `UserRepositoryAdapter implements UserRepository`)
- **Mappers**: `toDomain()` and `toEntity()` methods
- **Controllers**: Thin, delegate to use cases immediately

## Bounded Contexts

Modules within a service (e.g., `auth` and `user` in `identity-service`) are **separate bounded contexts**. They must not import from each other's domain or application layers.

**Rule**: If module A needs data from module B:
1. Module A defines its own **port interface** (application layer)
2. Module A defines its own **domain representation** (domain layer)
3. The **infrastructure layer can share** persistence entities (JPA entities, etc.)

```
❌ Wrong: auth imports from user's domain/application layers
   auth/application/services/LoginService.java
   └── import com.tickon.identity.user.application.ports.out.UserRepository;
   └── import com.tickon.identity.user.domain.User;

✅ Correct: auth defines its own port and domain model
   auth/application/ports/out/AuthUserRepository.java  → auth's own interface
   auth/domain/AuthUser.java                           → auth's view of user data (read-only projection)
   auth/infrastructure/persistence/AuthUserRepositoryAdapter.java → uses shared JpaUserRepository
```

**Infrastructure Sharing**: The adapter can import from user's infrastructure layer (e.g., `JpaUserRepository`, `UserEntity`) because:
- Infrastructure is where integration naturally happens
- Avoids duplicate JPA entities for the same table
- Single source of truth for database schema
- Proper handling of soft-delete filters, lifecycle callbacks, etc.

```
user module                              auth module
───────────                              ───────────
User (domain)                            AuthUser (domain)        ✅ Separate
UserRepository (port)                    AuthUserRepository (port) ✅ Separate
        │                                        │
        └──► JpaUserRepository / UserEntity ◄────┘               ✅ Shared in infrastructure
```

This ensures:
- Modules evolve independently at domain/application layers
- Each module owns its domain model
- No duplicate persistence entities for the same table

## TDD Workflow

1. **Write the failing test first** - Define expected behavior
2. **Make it pass** - Minimum code to satisfy the test
3. **Refactor** - Clean up while keeping tests green

### Test Conventions
- Test class: `{ClassName}Test`
- Test method: `should{ExpectedBehavior}_When{Condition}`
- Use `@ExtendWith(MockitoExtension.class)` for unit tests
- Use `ArgumentCaptor` to verify domain objects passed to repositories
- Use `verifyNoMoreInteractions()` to ensure no unexpected calls

## Commands

```bash
mvn test                           # Run all tests
mvn test -pl services/identity-service  # Run tests for specific service
mvn spotless:apply                 # Format code
mvn verify                         # Full build with checks
```

## Services

| Service | Port | Purpose |
|---------|------|---------|
| identity-service | 8082 | User registration, authentication, sessions |
| event-service | 8081 | Event management |
| api-gateway | 8080 | Routing, JWT validation |
| eureka-server | 8761 | Service discovery |

## See Also

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Detailed architecture guidelines
- [docs/TESTING.md](docs/TESTING.md) - TDD patterns and examples
