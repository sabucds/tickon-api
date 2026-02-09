# Tickon API — Claude Context

Ticket booking microservice platform (Spring Boot 3, Java 21, PostgreSQL).

## Non-negotiables (truth + quality)
- If unsure about codebase context, assumptions, or APIs: **ask for clarification**. **Do not invent code** or files.
- Prefer **quoting real references**: file paths, class names, method signatures.
- When fixing a bug, add a brief comment explaining intent:
  `// Fixed: Changed X to Y to prevent Z (as per spec/test).`

## Architecture (Hexagonal + DDD)
**Layers (dependency points inward):**
- `domain/` → Entities, Value Objects, Domain Events, Policies, Domain Exceptions
- `application/` → Use case interfaces (ports/in), services (use case impl), repo interfaces (ports/out), DTOs
- `infrastructure/` → Controllers, persistence/security adapters, mappers

Controllers are **thin**: validate input → call use case → return response.

## Domain rules
- Aggregates extend `AggregateRoot`
  - `create()` for new entities (registers events)
  - `restore()` for persistence rehydration (no events)
- Value Objects: **records** with validation in compact constructor
- Domain Events: records implementing `DomainEvent`, named `{Entity}{Action}Event`
- Exceptions: extend `DomainException` + `ErrorCode`

## Application rules
- One use case interface per operation (e.g. `RegisterUserUseCase`)
- Services orchestrate domain + publish events via `DomainEventPublisher`
- DTOs:
  - `*Command` input
  - `*Result` output

## Bounded Contexts (modules)
Modules inside a service are separate bounded contexts.
- ❌ No imports from another module’s `domain/`, `application/` or `infrastructure/`

### Cross-module reads: Query Bus only
✅ Use Query Bus for **synchronous, read-only** lookups across modules.  
❌ Not for writes. ❌ Not for async notifications (use domain events).

Queries live in `shared/contracts/queries/` and return `QueryResult<DTO>`.
Handlers live in owner module: `application/queryhandlers/`.

### Shared infrastructure only
Modules never import each other’s infrastructure.
Shared DB entities/repos go in `shared/infrastructure/`.

## TDD workflow (required)
1) Write failing test (behavior)  
2) Minimal code to pass  
3) Refactor safely (tests stay green)

### Test conventions
- Class: `{ClassName}Test`
- Method: `should{ExpectedBehavior}_When{Condition}`
- Unit tests: `@ExtendWith(MockitoExtension.class)`
- Use `ArgumentCaptor` for domain objects passed to repositories
- Use `verifyNoMoreInteractions()` to catch unexpected calls

## Commands
- `mvn test`
- `mvn test -pl services/identity-service`
- `mvn spotless:apply`
- `mvn verify`

## Services
- identity-service (8082): users/auth/sessions
- event-service (8081): events
- api-gateway (8080): routing/JWT validation
- eureka-server (8761): discovery

## References (read when needed)
- docs/ARCHITECTURE.md
- docs/TESTING.md