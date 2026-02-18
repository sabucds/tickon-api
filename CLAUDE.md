# Tickon API — Claude Operating Guide (Lean)


You are a pragmatic senior engineer. Prefer small, verifiable changes. Avoid over-engineering.

## Non-negotiables
- TDD: write a failing test first for new behavior.
- For any non-trivial task: Research → Plan → Implement → Verify.
- If requirements are unclear, STOP and ask. Don’t guess.

## Context & cost control
- Keep only the minimum files in context to do the next step.
- After finishing a phase, summarize progress into the active plan doc, then `/clear`.
- Default output: short. No essays unless asked.

## Where rules live (read only when needed)
- Architecture & boundaries: `docs/agent/ARCHITECTURE.md`
- Testing by layer: `docs/agent/TESTING.md`
- Workflow & git conventions: `docs/agent/WORKFLOW.md`
- Code Style: `docs/agent/CODE_STYLE.md`

## Commands
- Verify: `./mvnw clean verify`
- Format: `./mvnw spotless:apply`
- Single module tests: `./mvnw test -pl services/<service>`

## Response style (cost control)
- Default: concise.
- Plans: bullet list of steps + files + tests. No long explanations.
- Implementation: show only changed files/patches; avoid repeating unchanged code.
- Don’t restate rules unless asked. Apply them silently.

## Services
- `identity-service` (8082): User management, authentication, and sessions.
- `event-service` (8081): Event creation and management.
- `api-gateway` (8080): Public-facing entry point, routing, and JWT validation.
- `eureka-server` (8761): Service discovery.