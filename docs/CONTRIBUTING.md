# Contributing to Tickon API

We're excited that you want to contribute! This document provides guidelines for contributing to the Tickon API.

## Development Workflow

We follow a Trunk-Based Development model. All work is done in short-lived feature branches and merged into the `main` trunk.

### TDD is Required

We use Test-Driven Development (TDD). The workflow is as follows:

1.  **Write a failing test:** Before you write any implementation code, write a test that describes the desired behavior and fails because the functionality doesn't exist yet.
2.  **Write the minimal code to pass the test:** Write just enough code to make the failing test pass.
3.  **Refactor:** With the safety of passing tests, you can now refactor your code to improve its design, readability, and performance without changing its behavior.

This cycle ensures that our codebase is covered by tests and that the design evolves in a clean and maintainable way.

### Test Conventions

-   **Test Class Naming**: Test classes should be named `{ClassName}Test`.
-   **Test Method Naming**: Test methods should follow the pattern `should{ExpectedBehavior}_When{Condition}`.
-   **Unit Tests**: Use `@ExtendWith(MockitoExtension.class)` for unit tests. Mock dependencies to isolate the unit of work.
-   **`ArgumentCaptor`**: Use `ArgumentCaptor` to capture and assert arguments passed to mocked methods.
-   **Verification**: Use `verifyNoMoreInteractions()` to ensure that no unexpected methods were called on your mocks.

For more details on the testing strategy for each architectural layer, please read [TESTING.md](TESTING.md).

## Code Style and Formatting

We use [Spotless](https://github.com/diffplug/spotless) with the Google Java Format to maintain a consistent code style.

To format your code, run:

```bash
./mvnw spotless:apply
```

To check if your code is formatted correctly (as the CI server does), run:

```bash
./mvnw spotless:check
```

The `verify` command will also run the check:

```bash
./mvnw verify
```

## Running Tests

To run all tests in the project:

```bash
./mvnw test
```

To run tests for a specific service (e.g., `identity-service`):

```bash
./mvnw test -pl services/identity-service
```

## Commit Messages

Please follow the [Conventional Commits](https://www.conventionalcommits.org/) specification. This helps us automate versioning and changelog generation.

**Format:**

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Example:**

```
feat(identity): add endpoint for user registration

Implements the user registration use case, including validation and password hashing.

Fixes #42
```

## Pull Requests

-   Your PR should be focused on a single logical change.
-   Ensure all tests are passing (`./mvnw verify`).
-   Your PR title should follow the Conventional Commits format.
-   Link any relevant issues in your PR description.
-   Update documentation if you are changing behavior.
