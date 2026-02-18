# Testing Guidelines (Canonical)

Tests define behavior. Prefer tests that catch regressions with minimal brittleness.

## Required baseline
- New behavior requires tests.
- Bug fixes require a regression test (fails before fix, passes after).
- Always run `./mvnw clean verify` before considering work done.

## What to test (by layer)

### Domain (highest ROI)
Test:
- value object validation
- aggregate invariants and behavior methods
- domain events emitted
- policy edge cases

Avoid:
- mocking domain objects (use real instances)

### Application (use case orchestration)
Test:
- happy path flow
- domain/policy validation failures
- interactions with output ports (repo, hasher, publisher, token provider)
- events published (and cleared if your design requires it)

Mock:
- output ports (repositories, publishers, providers)

Avoid:
- asserting internal call order unless required
- over-specifying interactions that don’t matter to behavior

### Infrastructure (only when there is logic)
Test:
- mapping logic (domain ↔ persistence/web)
- tricky adapter behavior
- limited integration tests where wiring matters:
  - controllers with validation
  - persistence behavior (optionally Testcontainers)

Controllers:
- Prefer a small number of MockMvc integration tests for request validation and wiring.
- Do not unit-test controllers unless they contain logic.

## Test conventions
- Test class naming: `<ClassName>Test`
- Test method naming: `should<Expected>_When<Condition>`
- Structure: Arrange → Act → Assert
- Mockito unit tests: `@ExtendWith(MockitoExtension.class)`

### Interaction assertions
- Use `ArgumentCaptor` when verifying persisted/published content matters.
- Use `verifyNoMoreInteractions()` only when it adds signal (avoid brittle tests).

## TDD loop (required for new behavior)
1) write failing test
2) implement minimal code to pass
3) refactor with tests green

## Example (short)
```java
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordHasher passwordHasher;
  @Mock DomainEventPublisher eventPublisher;

  RegisterUserService service;

  @BeforeEach
  void setUp() {
    service = new RegisterUserService(
        userRepository,
        passwordHasher,
        new PasswordStrengthPolicy(),
        eventPublisher
    );
  }

  @Test
  void shouldRegisterUser_WhenValidInput() {
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    var result = service.register(aCommand("john@example.com", "SecurePass123!"));

    var captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    verify(eventPublisher).publishAll(anyList());

    assertThat(result.email()).isEqualTo("john@example.com");
    assertThat(captor.getValue().email().value()).isEqualTo("john@example.com");
  }

  @Test
  void shouldThrowDuplicateEmail_WhenEmailExists() {
    when(userRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> service.register(aCommand("john@example.com", "x")))
        .isInstanceOf(DuplicateEmailException.class);

    verifyNoInteractions(passwordHasher, eventPublisher);
  }

  private RegisterUserCommand aCommand(String email, String pwd) {
    return new RegisterUserCommand(Email.from(email), pwd);
  }
}
```

## Agent cost controls
Do not paste long templates into chat output.
- Write only tests relevant to the change.
- Prefer 1–3 high-impact edge-case tests over enumerating every possibility.