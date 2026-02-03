# Testing Guidelines

## TDD Workflow

### Red-Green-Refactor Cycle

1. **RED**: Write a failing test that defines expected behavior
2. **GREEN**: Write minimum code to make the test pass
3. **REFACTOR**: Clean up code while keeping tests green

### When Implementing a New Feature

```
1. Write use case interface test → implement service
2. Write domain tests → implement aggregate/value object
3. Write adapter tests (if complex logic) → implement adapter
4. Write controller integration test → implement controller
```

## Test Structure

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private DomainEventPublisher eventPublisher;

    private PasswordStrengthPolicy passwordPolicy;
    private RegisterUserService registerUser;

    @BeforeEach
    void setUp() {
        passwordPolicy = new PasswordStrengthPolicy();
        registerUser = new RegisterUserService(userRepository, passwordHasher, passwordPolicy, eventPublisher);
    }

    @Test
    void shouldRegisterUser_WhenValidInput() {
        // Arrange
        RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        when(passwordHasher.hash("SecurePass123!")).thenReturn(new PasswordHash("hashed"));

        // Act
        UserResult result = registerUser.register(command);

        // Assert
        assertThat(result.username()).isEqualTo("john_doe");
        assertThat(result.email()).isEqualTo("john@example.com");

        // Verify interactions
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.email().value()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("Email already in use: john@example.com");

        verifyNoMoreInteractions(passwordHasher);
    }

    // Test fixture helper
    private RegisterUserCommand aCommand(String username, String email, String password) {
        return new RegisterUserCommand("John", "Doe", new Username(username), new Email(email), password);
    }
}
```

## Test Naming Convention

```
should{ExpectedBehavior}_When{Condition}
```

Examples:
- `shouldRegisterUser_WhenValidInput`
- `shouldThrowException_WhenEmailAlreadyExists`
- `shouldReturnEmpty_WhenUserNotFound`
- `shouldPublishUserCreatedEvent_WhenRegistrationSucceeds`

## What to Test

### Domain Layer

| Component | What to Test |
|-----------|--------------|
| Value Objects | Validation rules, equality, factory methods |
| Aggregates | Factory methods, business methods, invariants, domain events |
| Policies | All validation rules and edge cases |
| Domain Exceptions | Message formatting, error codes |
| Domain Events | Event data, occurredOn timestamp |

```java
class EmailTest {
    @Test
    void shouldCreateEmail_WhenValid() {
        Email email = new Email("test@example.com");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowException_WhenInvalidFormat() {
        assertThatThrownBy(() -> new Email("invalid"))
            .isInstanceOf(InvalidEmailException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a@b.co", "user.name+tag@domain.com", "test@sub.domain.org"})
    void shouldAcceptValidEmails(String email) {
        assertThatCode(() -> new Email(email)).doesNotThrowAnyException();
    }
}
```

### Testing Domain Events

```java
class UserTest {
    @Test
    void shouldRegisterUserCreatedEvent_WhenCreated() {
        User user = User.create(
            UserId.generate(),
            new Email("test@example.com"),
            new Username("testuser"),
            "John",
            "Doe",
            new PasswordHash("hashed")
        );

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().get(0)).isInstanceOf(UserCreatedEvent.class);

        UserCreatedEvent event = (UserCreatedEvent) user.domainEvents().get(0);
        assertThat(event.userId()).isEqualTo(user.id());
        assertThat(event.email()).isEqualTo(user.email());
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void shouldNotRegisterEvents_WhenRestored() {
        User user = User.restore(
            UserId.generate(),
            new Email("test@example.com"),
            new Username("testuser"),
            "John",
            "Doe",
            new PasswordHash("hashed"),
            UserStatus.ACTIVE
        );

        assertThat(user.domainEvents()).isEmpty();
    }

    @Test
    void shouldRegisterEmailChangedEvent_WhenEmailChanged() {
        User user = User.create(...);
        user.clearEvents(); // Clear creation event

        Email newEmail = new Email("new@example.com");
        user.changeEmail(newEmail);

        assertThat(user.domainEvents()).hasSize(1);
        assertThat(user.domainEvents().get(0)).isInstanceOf(UserEmailChangedEvent.class);
    }
}
```

### Application Layer

| Component | What to Test |
|-----------|--------------|
| Services | Business flow, exception handling, port interactions, event publishing |

```java
class RegisterUserServiceTest {
    @Test
    void shouldPublishDomainEvents_WhenRegistrationSucceeds() {
        // Arrange
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

        // Act
        registerUser.register(aCommand("user", "user@test.com", "SecurePass123!"));

        // Assert
        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).publishAll(eventsCaptor.capture());

        List<DomainEvent> events = eventsCaptor.getValue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(UserCreatedEvent.class);
    }
}
```

### Infrastructure Layer

| Component | What to Test |
|-----------|--------------|
| Adapters | Complex mapping logic, edge cases |
| Controllers | Integration tests with MockMvc (optional) |

## Mocking Guidelines

### What to Mock
- Output ports (repositories, external services, event publishers)
- Infrastructure concerns (time, random generators)

### What NOT to Mock
- Domain objects (use real instances)
- Policies (use real instances unless testing service in isolation)
- Value objects (always use real instances)

### Using ArgumentCaptor

```java
@Test
void shouldSaveUserWithHashedPassword() {
    // Arrange
    when(passwordHasher.hash("raw")).thenReturn(new PasswordHash("hashed"));
    when(userRepository.existsByEmail(any())).thenReturn(false);

    // Act
    registerUser.register(aCommand("user", "user@test.com", "raw"));

    // Assert - capture and verify the saved user
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User saved = captor.getValue();
    assertThat(saved.passwordHash().value()).isEqualTo("hashed");
}
```

### Verifying No Unwanted Interactions

```java
@Test
void shouldNotHashPassword_WhenEmailExists() {
    when(userRepository.existsByEmail(any())).thenReturn(true);

    assertThatThrownBy(() -> registerUser.register(command))
        .isInstanceOf(DuplicateEmailException.class);

    // Verify password hasher was never called
    verifyNoMoreInteractions(passwordHasher);
    verifyNoInteractions(eventPublisher);
}
```

## Test Fixtures

Create helper methods for common test data:

```java
class UserFixtures {
    public static User aUser() {
        return User.create(
            UserId.generate(),
            new Email("test@example.com"),
            new Username("testuser"),
            "John",
            "Doe",
            new PasswordHash("hashed")
        );
    }

    public static User aUserWith(Email email) {
        return User.create(
            UserId.generate(),
            email,
            new Username("testuser"),
            "John",
            "Doe",
            new PasswordHash("hashed")
        );
    }
}
```

## Integration Tests (Optional)

For testing persistence adapters with real database:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private JpaUserRepository jpaRepository;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpaRepository, new UserPersistenceMapper());
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User user = UserFixtures.aUser();

        adapter.save(user);
        Optional<User> found = adapter.findById(user.id());

        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo(user.email());
    }
}
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific service tests
mvn test -pl services/identity-service

# Run specific test class
mvn test -pl services/identity-service -Dtest=RegisterUserServiceTest

# Run with coverage report
mvn verify
# Report at: target/site/jacoco/index.html
```
