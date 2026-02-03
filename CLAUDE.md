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
| identity-service | 8081 | User registration, authentication, sessions |
| event-service | 8082 | Event management |
| api-gateway | 8080 | Routing, JWT validation |
| eureka-server | 8761 | Service discovery |

## See Also

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Detailed architecture guidelines
- [docs/TESTING.md](docs/TESTING.md) - TDD patterns and examples
