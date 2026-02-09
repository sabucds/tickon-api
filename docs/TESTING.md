# Testing Guidelines

We follow **TDD**: Red → Green → Refactor.

## What to test (by layer)

### Domain (highest ROI)
Test:
- Value object validation
- Aggregate invariants + behavior methods
- Domain events emitted
- Policy edge cases

Avoid:
- Mocking domain objects (use real instances)

### Application (use case orchestration)
Test:
- happy path flow
- exception cases
- interactions with output ports
- domain events published (and cleared if your design requires it)

Mock:
- output ports (repositories, event publisher, token provider, hasher)

### Infrastructure (only when it has logic)
Test:
- complex mapping
- tricky adapter behavior
- optional integration tests (JPA/Testcontainers)

Controllers: prefer a small number of integration tests (MockMvc) for wiring + validation.

## Conventions
- Class: `{ClassName}Test`
- Method: `should{ExpectedBehavior}_When{Condition}`

## Unit test template (service)
- Use `@ExtendWith(MockitoExtension.class)`
- Arrange → Act → Assert
- Use `ArgumentCaptor` to verify what you persisted/published
- Use `verifyNoMoreInteractions()` / `verifyNoInteractions()` to detect accidental behavior

### Example (short)
```java
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordHasher passwordHasher;
  @Mock DomainEventPublisher eventPublisher;

  RegisterUserService service;

  @BeforeEach
  void setUp() {
    service = new RegisterUserService(userRepository, passwordHasher, new PasswordStrengthPolicy(), eventPublisher);
  }

  @Test
  void shouldRegisterUser_WhenValidInput() {
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    var result = service.register(aCommand("john@example.com", "SecurePass123!"));

    var captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    verify(eventPublisher).publishAll(anyList());
    verifyNoMoreInteractions(passwordHasher, userRepository, eventPublisher);

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
    return new RegisterUserCommand(new Email(email), pwd);
  }
}
