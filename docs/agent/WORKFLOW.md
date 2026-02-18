# Development Workflow (Canonical)

This is the canonical workflow for implementing features and fixing bugs in Tickon API.
Keep changes small, verifiable, and within the architecture boundaries.

## Default loop (non-trivial work)
For anything beyond a tiny change, use this loop:

1) Research
2) Plan
3) Implement (TDD)
4) Verify

Do not skip steps.

## Context & cost control (required)
- Keep only the minimum files in context to perform the next step.
- After each chunk of work, write a short progress note into the active plan doc, then `/clear`.
- Default responses must be concise. No long explanations unless asked.

## Plans (required for non-trivial work)
- Every non-trivial task must have a plan file:
  - `docs/plans/<YYYY-MM-DD>-<topic>.md`
- A plan is the project memory. Prefer updating the plan over keeping long chat context.

### Plan template (copy into each plan)
```text
# <ticket> <feature>
Status: planned | in-progress | done
Service/module: <...>

## Goal
## Scope / Non-goals
## Touchpoints (files/packages)
## Tests to write first
## Steps (execute 2–4 at a time)
## Verification (exact commands)
## Progress log (brief)
```

## ADRs (architecture decisions)
Any change affecting:
- boundaries, module relationships, shared contracts shape
- new infra dependencies (auth, crypto, messaging, persistence)
- new cross-module communication patterns

Requires an ADR in `docs/adr/`.

## Phase 1 — Research

**Objective:** understand the existing code and constraints.

- Read only the relevant files (use `@` mentions selectively).
- Search the codebase for similar patterns before inventing new ones.
- Identify:
  - which service and bounded context (module) owns the change
  - which layer(s) it belongs in (`domain` / `application` / `infrastructure`)
  - whether cross-module communication is needed (QueryBus / CommandBus / Domain Events)

**Output:** a short plan draft (or plan updates) in the plan file.

## Phase 2 — Plan

**Objective:** write a step-by-step plan that is easy to execute and verify.

**Plan requirements:**
- steps are numbered and small
- each step names files/packages to touch
- includes the first failing test(s) to write
- includes exact verification commands
- includes rollback notes if risk exists
- includes only 1–3 high-impact edge cases as tests (don’t enumerate everything)

Stop after writing the plan and wait for approval if the task changes architecture, boundaries, or introduces new dependencies.

## Phase 3 — Implement (TDD)

**Objective:** implement in small batches.

**Rules:**
- TDD is mandatory for new behavior:
  1) write a failing test  
  2) implement minimal code to pass  
  3) refactor with tests green
- Execute 2–4 plan steps, then stop.
- Do not do drive-by refactors. Record them as follow-ups in the plan.

## Phase 4 — Verify

**Objective:** ensure correctness and cleanliness.

**Required:**
- `./mvnw clean verify`

If formatting fails:
- `./mvnw spotless:apply`
- then `./mvnw clean verify` again

## Git workflow (trunk-based)

Use short-lived branches:
- `feat/<short>`
- `fix/<short>`
- `docs/<short>`

Commit frequently (small commits).

Conventional Commits for messages:
- `feat(scope): ...`
- `fix(scope): ...`
- `test(scope): ...`
- `refactor(scope): ...`
- `chore(scope): ...`

## Debugging rules (systematic)

- Read the error message.
- Reproduce consistently.
- Find similar working code.
- Form one hypothesis at a time; test minimally.
- Avoid multi-fix “shotgun” commits.

## Quick commands

- Verify all: `./mvnw clean verify`
- Format: `./mvnw spotless:apply`
- One service tests: `./mvnw test -pl services/<service>`
