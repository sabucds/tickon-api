# Tickon API — Claude Context

This document is a high-level summary for developers. For detailed guides, see the `docs/` directory.

## Workflow: How to Approach Tasks

**For ANY non-trivial task, follow this workflow:**

1. **Research** - Read relevant files to understand the current implementation. Use `@` mentions to reference specific files if needed.
2. **Plan** - Propose a clear, step-by-step plan. Consider edge cases. Wait for approval before implementing.
3. **Implement** - Use TDD: write failing tests first, then implement to pass them.
4. **Validate** - Run `mvn clean verify` to ensure tests pass and code style is correct.

**TDD is mandatory**: Always write tests before implementation. Tests define the contract and provide a verifiable target.

**Context management**: Use `/clear` when switching to a new logical task. Keep context focused on the current work.

**Edge cases**: During planning, articulate potential edge cases (null values, invalid states, concurrency issues, boundary conditions).

**For guidance on specific topics, read these docs BEFORE starting work:**
- Architecture patterns & module boundaries → [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- Testing strategy by layer → [docs/TESTING.md](docs/TESTING.md)
- Detailed workflow, git conventions, commit format → [docs/WORKFLOW.md](docs/WORKFLOW.md)

## Architecture (Hexagonal + DDD)

**Layers (dependency points inward):**
- `domain/` → Business rules, entities, value objects. No frameworks.
- `application/` → Use case orchestration, ports (interfaces).
- `infrastructure/` → Frameworks, adapters (DB, web), configuration.

**Key principle:** Services contain multiple internal modules (Bounded Contexts). Modules are isolated - no compile-time dependencies between modules. Communication via QueryBus (sync, read-only) or Domain Events (async).

## Essential Commands

- `mvn clean verify` - Run all tests and style checks. Use this to validate your changes.
- `mvn spotless:apply` - Auto-format code. Run before committing.
- `mvn test -pl services/identity-service` - Run tests for a specific service.

## Services

- `identity-service` (8082): User management, authentication, and sessions.
- `event-service` (8081): Event creation and management.
- `api-gateway` (8080): Public-facing entry point, routing, and JWT validation.
- `eureka-server` (8761): Service discovery.