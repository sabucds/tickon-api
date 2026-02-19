# Development Workflow (Short Canon)

Use for any non-trivial task. Keep changes small, testable, and within boundaries.

## Required loop (don’t skip)
1) Research
2) Plan
3) Implement (TDD)
4) Verify

## Context rules
- Open/read the minimum files needed for the current step.
- Work in small chunks (2–4 steps), then update the plan and clear context.
- Keep responses concise.

## Plan files (required)
- Create/update: `docs/plans/<YYYY-MM-DD>-<topic>.md`
- Plans are the project memory.

Plan template (copy into plan file):
```text
# <ticket> <feature>
Status: planned | in-progress | done
Service/module: <...>

## Goal
## Scope / Non-goals
## Touchpoints (files/packages)
## Tests to write first
## Steps (2–4 at a time)
## Verification (exact commands)
## Progress log (brief)
```

## ADR rule
If you change boundaries, module relationships, shared contracts, infra dependencies, or cross-module patterns: add an ADR in `docs/adr/<NNNN>-<short>-<title>.md`.

## Research
- Read relevant files only; reuse existing patterns.
- Identify: owning service/bounded context, layer (domain/application/infrastructure), cross-module needs.
- Output: update the plan.

## Plan
Plan must include:
- Numbered steps + files/packages per step
- First failing tests
- Exact verification commands
- Rollback note if risky
- 1–3 edge cases as tests
If the plan changes architecture/boundaries/dependencies: stop and request approval before coding.

## Implement (TDD)
- New behavior: failing test → minimal code → refactor with tests green.
- Execute only 2–4 plan steps, then stop and update the plan.
- No drive-by refactors; log follow-ups in the plan.

## Verify (commands)
- `./mvnw clean verify`
- If formatting fails: `./mvnw spotless:apply` then `./mvnw clean verify`
- Single service tests: `./mvnw test -pl services/<service>`

## Git (trunk-based)
- Short-lived branches: `feat/<short>`, `fix/<short>`, `docs/<short>`
- Small commits; Conventional commits: `feat`, `fix`, `test`, `refactor`, `chore` (optional scope)

## Debugging
- Read the error, reproduce, compare with working code.
- Change one thing at a time; avoid shotgun commits.
