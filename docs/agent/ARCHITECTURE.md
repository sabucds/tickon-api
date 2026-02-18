# Architecture Guidelines (Canonical)

We use Hexagonal Architecture + DDD with vertical slices (bounded contexts).
Keep boundaries strict. If boundaries blur, the system rots.

## Core rule: dependencies point inward
Infrastructure → Application → Domain

- Domain must not depend on Spring, HTTP, DB, security libraries, or framework annotations.
- Application must not depend on infrastructure implementations.
- Infrastructure depends on application and domain.

## Services and modules
- A service contains multiple modules (bounded contexts).
- Modules are isolated:
  - ❌ no compile-time imports between modules' `domain/`, `application/`, or `infrastructure/`
  - ❌ no direct imports from other modules' domain (e.g., `com.tickon.<service>.<module>.domain.*`)
  - ✅ shared contracts live under `shared/contracts/`
  - ✅ shared infrastructure primitives live under `shared/infrastructure/`
  - ✅ shared abstractions (not owned models) may live under `shared/domain/`
  - ✅ truly shared value objects (Email, UserId, etc.) belong in the common module: `com.tickon.common.identity.domain.valueobjects.*`

## Package structure (example)
`com.tickon.<service>/`
- `<module>/domain/`
- `<module>/application/`
  - `ports/in/`
  - `ports/out/`
  - `services/`
  - `dto/`
  - `queryhandlers/` (if used)
  - `commandhandlers/` (if used)
- `<module>/infrastructure/`
  - `web/`
  - `persistence/`
  - `security/`
  - `events/`
- `shared/`
  - `contracts/queries/`
  - `contracts/commands/`
  - `infrastructure/`
  - `domain/`
- `config/`

## Domain rules (DDD discipline)
### Aggregates
- No public setters.
- Factories:
  - `create()` for new entities (emit domain events)
  - `restore()` for rehydration (no events)
- Behavior methods enforce invariants and emit domain events.

### Value Objects
- Prefer `record`.
- Validate in the compact constructor or explicit factory.
- Use intent-revealing factories (e.g., `Email.from(...)`) when it clarifies.

### Domain Events
- Records implementing `DomainEvent`.
- Naming: `<Entity><Action>Event`.
- Capture timestamp at construction.

### Exceptions
- Domain exceptions extend `<Service>DomainException`.
- Include a stable `ErrorCode`.

## Application rules (use cases are the API)
- One use case interface per operation: `RegisterUserUseCase`.
- Use case implementation coordinates:
  1) validation + policy checks
  2) domain operation
  3) persistence via output ports
  4) publish domain events (via output port)
- Application has no direct Spring/JPA/web dependencies.

## Infrastructure rules
- Controllers: validate → call use case → map response. No business logic.
- Adapters implement output ports (e.g., repository adapter, token provider).
- Mappers are explicit:
  - `toDomain()` / `toEntity()` / `toDto()`
- Prefer constructor injection.
- Framework configuration stays in infrastructure/config.

## Cross-module communication (inside a service)
Modules communicate using only one of these patterns:

### 1) QueryBus (sync reads)
Use for read-only queries across modules.
- Synchronous, read-only, type-safe.
- Returns `QueryResult<T>`.

Contracts:
- Query contract: `shared/contracts/queries/`
- Query handler: `<target-module>/application/queryhandlers/`

### 2) CommandBus (sync writes)
Use for critical state changes that must be atomic with the caller.
- Synchronous, transactional, type-safe.
- Returns `CommandResult<T>`.
- Use sparingly (couples modules).

Contracts:
- Command contract: `shared/contracts/commands/`
- Command handler: `<target-module>/application/commandhandlers/`

### 3) Domain Events (async notifications)
Use for non-critical side effects and decoupled reactions.
- Asynchronous, eventual consistency.
- Fire-and-forget.

Files:
- Event: `<source-module>/domain/events/`
- Handler: `<listener-module>/infrastructure/events/`

## Decision rules
- Reads across modules: QueryBus.
- Critical writes across modules: CommandBus.
- Side effects: Domain Events.
- Do not introduce new cross-module mechanisms without an ADR.
