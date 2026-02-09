# Development Workflow

This guide provides a detailed workflow for developing features and fixing bugs in the Tickon API. For general contribution guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

## The Four-Phase Workflow

For any non-trivial task (more than a simple bug fix), follow this structured approach:

### Phase 1: Research

**Objective:** Understand the current implementation and related code.

1. Read relevant files using `@` mentions to include them in context
2. Search for similar patterns in the codebase using Grep/Glob
3. Understand the architecture layer where your change belongs (domain/application/infrastructure)
4. Identify module boundaries and dependencies

**Context tip:** Use `/context` to check your token usage. Keep research focused. Use `/clear` before moving to the next phase if context exceeds 60%.

### Phase 2: Plan

**Objective:** Design a clear, step-by-step implementation approach.

1. Propose a plan with numbered steps
2. Consider edge cases:
   - Null or invalid values
   - Boundary conditions (empty lists, max/min values)
   - Invalid states (end date before start date, negative quantities)
   - Concurrency issues (two users editing the same data)
3. Identify which tests need to be written first
4. Wait for approval before implementing

**Example plan format:**
```
Plan for implementing user logout:
1. Write failing test: should invalidate session when user logs out
2. Create LogoutUserCommand in application layer
3. Implement LogoutUserService with session repository dependency
4. Create infrastructure adapter for session repository
5. Add REST controller endpoint
6. Verify with `mvn clean verify`
```

**Planning tip:** For complex features, use Claude's extended thinking mode. You can trigger this by saying "think harder" or "ultrathink" in your prompt.

### Phase 3: Implement (TDD)

**Objective:** Write clean, tested code following TDD.

**The TDD Cycle:**

1. **Write a failing test**
   - Test should describe the desired behavior
   - Use the naming convention: `should{ExpectedBehavior}_When{Condition}`
   - Verify it fails by running the test

2. **Write minimal code to pass the test**
   - Implement just enough to make the test pass
   - Follow the architectural layering (domain → application → infrastructure)
   - Respect module boundaries (no compile-time dependencies between modules)

3. **Refactor**
   - Improve code design while keeping tests green
   - Extract reusable components if needed
   - Ensure code is readable and maintainable

**Repeat the cycle** for each piece of functionality.

**Testing tip:** See [TESTING.md](TESTING.md) for layer-specific testing strategies.

### Phase 4: Validate

**Objective:** Ensure code quality and style compliance.

1. Run all tests: `mvn clean verify`
2. Fix any test failures or style violations
3. If style violations exist, run: `mvn spotless:apply`
4. Verify again: `mvn clean verify`

**Context management:** Use `/clear` after validation if starting a new feature.

## Context Window Management

Claude Code has a 200k token context window. A fresh session in this project uses ~20k tokens (10%) with the remaining 180k for making changes.

**Best practices:**

- Run `/context` mid-session to check usage
- **Never exceed 60% context** - quality degrades beyond this point
- Use `/clear` when switching to a new logical task
- Use the "Document & Clear" method for complex tasks:
  1. Have Claude dump the plan and progress into a `PLAN.md` file
  2. Use `/clear` to reset context
  3. Start a new session by reading the `PLAN.md` and continuing

**Context tip:** Split work into the four phases and clear context between phases for best results.

## Git Workflow

### Branching Strategy

We use Trunk-Based Development with short-lived feature branches.

**Branch naming:**
- Features: `feat/short-description` (e.g., `feat/user-logout`)
- Fixes: `fix/short-description` (e.g., `fix/session-timeout`)
- Docs: `docs/short-description` (e.g., `docs/update-api-guide`)

**Creating a branch:**
```bash
git checkout -b feat/your-feature-name
```

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

**Format:**
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring without behavior change
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Example:**
```
feat(identity): add logout endpoint

Implements the logout use case with session invalidation.
Tests verify that sessions are properly removed from the repository.

Fixes #42
```

### Pull Requests

1. Ensure all tests pass: `./mvnw verify`
2. Push your branch: `git push -u origin feat/your-feature-name`
3. Create a PR with:
   - Title following Conventional Commits format
   - Clear description of changes
   - Link to relevant issues
   - Notes on testing performed
4. Wait for review and address feedback

## Troubleshooting Approach

When encountering issues:

1. **Read error messages carefully** - they often contain the solution
2. **Search for similar working code** - find patterns in the codebase
3. **Form a hypothesis** - what do you think is the root cause?
4. **Test minimally** - make the smallest change to test your hypothesis
5. **Verify before continuing** - don't add more fixes if the first didn't work

**Never:**
- Fix multiple things at once
- Proceed without understanding the root cause
- Add workarounds instead of fixing the underlying issue

## Edge Case Checklist

When planning or implementing, consider:

- ✓ Null or missing values
- ✓ Empty collections
- ✓ Boundary values (0, negative numbers, max integers)
- ✓ Invalid state transitions
- ✓ Duplicate operations (e.g., creating the same entity twice)
- ✓ Concurrent modifications
- ✓ Authorization edge cases (accessing resources you don't own)
- ✓ Network failures (for external integrations)

**Tip:** Document edge cases as test cases. If you identify an edge case, write a test for it.

## Quick Reference

| Task | Command |
|------|---------|
| Run all tests | `./mvnw clean verify` |
| Run tests for one service | `./mvnw test -pl services/identity-service` |
| Format code | `./mvnw spotless:apply` |
| Check context usage | `/context` |
| Clear context | `/clear` |
| Create feature branch | `git checkout -b feat/feature-name` |

## Related Documentation

- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture patterns and principles
- [TESTING.md](TESTING.md) - Testing strategy by layer
