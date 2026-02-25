# Architecture Guidelines (Canonical)

We use Hexagonal Architecture + DDD with vertical slices (bounded contexts) and CQRS via an in-process bus.
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
  - ✅ shared contracts live under `contracts/<source-module>/commands/` and `contracts/<source-module>/queries/`
  - ✅ shared infrastructure primitives live under `shared/platform/`
  - ✅ shared pure-Java abstractions live under `shared/kernel/`
  - ✅ truly shared value objects (Email, UserId, etc.) belong in the common module: `com.tickon.common.identity.domain.valueobjects.*`

## Package structure (identity-service example)
```
com.tickon.<service>/
  <module>/
    domain/
      events/
      exceptions/
      policies/
      valueobjects/
    application/
      ports/              ← output port interfaces (repo, publisher, etc.)
      command/
        <operation>/          ← one sub-package per command (vertical slice)
          <Operation>Command.java
          <Operation>CommandHandler.java
      query/
        <operation>/          ← one sub-package per query (vertical slice)
          <Operation>Query.java
          <Operation>QueryHandler.java
      <SharedResult>.java     ← shared result types used across slices in this module
    infrastructure/
      web/
        dto/
        mappers/
      persistence/
      security/
      events/
  shared/
    kernel/                   ← pure Java, zero Spring
      ports/              ← shared output port interfaces (e.g., PasswordHasher)
      exceptions/
    platform/                 ← Spring-aware shared glue
      bus/                    ← CommandBus / QueryBus implementations
      metrics/
      web/
  contracts/
    <source-module>/
      commands/               ← Command types dispatched cross-module via CommandBus
      queries/                ← Query types dispatched cross-module via QueryBus
  bootstrap/
    config/                   ← Spring wiring only (beans, security config, etc.)
```

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

## Application rules (CQRS via handlers)
- No UseCase interfaces. Operations are expressed as Commands and Queries.
- Each command/query lives in its own vertical slice: `application/command/<operation>/` or `application/query/<operation>/`.
- A `CommandHandler` or `QueryHandler` per slice coordinates:
  1. validation + policy checks
  2. domain operation
  3. persistence via output ports
  4. publish domain events (via output port)
- Application has no direct Spring/JPA/web dependencies (handlers may use `@Component` / `@Transactional` as infrastructure concerns via Spring's annotation model, but no framework logic).

## Infrastructure rules
- Controllers: validate → dispatch command/query via bus → map response. No business logic.
- Controllers inject `CommandBus` and/or `QueryBus` directly. No UseCase interfaces.
- Adapters implement output ports (e.g., repository adapter, token provider).
- Mappers are explicit:
  - `toDomain()` / `toEntity()` / `toDto()`
- Prefer constructor injection.
- Framework configuration stays in `bootstrap/config/`.

## Cross-module communication (inside a service)
Modules communicate using only one of these patterns:

### 1) QueryBus (sync reads)
Use for read-only queries across modules.
- Synchronous, read-only, type-safe.
- Returns `QueryResult<T>`.

Contracts:
- Query contract: `contracts/<source-module>/queries/`
- Query handler: `<target-module>/application/query/<operation>/`

### 2) CommandBus (sync writes)
Use for critical state changes that must be atomic with the caller.
- Synchronous, transactional, type-safe.
- Returns `CommandResult<T>`.
- Use sparingly (couples modules).

Contracts:
- Command contract: `contracts/<source-module>/commands/`
- Command handler: `<target-module>/application/command/<operation>/`

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
