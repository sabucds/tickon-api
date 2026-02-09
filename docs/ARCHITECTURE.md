# Architecture Guidelines

We use **Hexagonal Architecture + DDD**. Keep boundaries strict or the codebase turns into a bowl of spaghetti with Spring annotations on top.

## Core rule: dependencies point inward
**Infrastructure → Application → Domain**  
Domain must not depend on Spring, DB, HTTP, security libs, etc.

## Layers and responsibilities

### Domain (`{module}/domain`)
Owns business meaning.
- Aggregates, Value Objects, Policies, Domain Events, Domain Exceptions
- No frameworks. No persistence. No web. No DTOs.

### Application (`{module}/application`)
Owns orchestration.
- Input ports (use case interfaces)
- Use case implementations (services)
- Output ports (repositories, hashing, tokens, event publishing)
- DTOs: `*Command` (input), `*Result` (output)

### Infrastructure (`{module}/infrastructure`)
Owns integration details.
- Controllers (web)
- Adapters implementing output ports (JPA, JWT, Argon2, etc.)
- Mappers: domain ↔ persistence/web models
- Spring configuration glue

**Controller rule:** validate → call use case → map response. No business logic.

## Package structure (example)
com.tickon.{service}/
├── {module}/
│ ├── domain/
│ ├── application/
│ │ ├── ports/in/
│ │ ├── ports/out/
│ │ ├── services/
│ │ ├── dto/
│ │ └── queryhandlers/
│ └── infrastructure/
│ ├── web/
│ ├── persistence/
│ └── security/
├── shared/
│ ├── contracts/queries/
│ ├── infrastructure/
│ └── domain/ # only abstractions, not module-owned models
└── config/

## Domain rules (DDD discipline)

### Aggregates
- No public setters.
- Use factories:
  - `create()` for new entities (register events)
  - `restore()` for rehydration (no events)
- Behavior methods emit domain events.

### Value Objects
- Use **records**.
- Validate in the compact constructor.
- Prefer factories like `Email.from(...)` when it clarifies intent.

### Domain Events
- Records implementing `DomainEvent`
- Naming: `{Entity}{Action}Event`
- Timestamp captured at construction.

### Exceptions
- Domain exceptions extend `{Service}DomainException` and include an `ErrorCode`.

## Application rules (use cases are the API)
- 1 interface per operation: `RegisterUserUseCase`
- Service implements the use case and coordinates:
  1) validations + policy checks
  2) domain operation
  3) persistence
  4) publish events
- Output ports only (no direct Spring/JPA usage in application)

## Infrastructure rules
- Adapters implement ports (e.g., `UserRepositoryAdapter implements UserRepository`)
- Mappers: `toDomain()` / `toEntity()` (no “magic” mapping)
- Prefer constructor injection only.

## Bounded contexts (modules)
Modules inside a service (e.g., `auth`, `user`) are **separate bounded contexts**.

### Hard import rule
- ❌ A module must never import another module’s `domain/`, `application/`, or `infrastructure/`.
- ✅ Shared contracts live in `shared/contracts/`.
- ✅ Shared persistence (entities/repos) lives in `shared/infrastructure/`.
