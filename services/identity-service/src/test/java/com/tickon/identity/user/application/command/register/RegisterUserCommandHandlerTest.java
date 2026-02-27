package com.tickon.identity.user.application.command.register;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.ports.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.events.UserCreatedEvent;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.exceptions.InvalidPasswordException;
import com.tickon.identity.user.domain.exceptions.PasswordViolation;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.Username;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserCommandHandlerTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordHasher passwordHasher;

  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private PasswordStrengthPolicy passwordPolicy;

  private RegisterUserCommandHandler handler;

  @BeforeEach
  void setUp() {
    passwordPolicy = new PasswordStrengthPolicy();
    handler = new RegisterUserCommandHandler(userRepository, passwordHasher, passwordPolicy, eventPublisher, metrics);
  }

  @Test
  void shouldRegisterUser_WhenValidInput() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    PasswordHash hashedPassword = new PasswordHash("hashed-password");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash("SecurePass123!")).thenReturn(hashedPassword.value());

    UserResult result = handler.handle(command).orElseThrow();

    assertThat(result.username()).isEqualTo("john_doe");
    assertThat(result.email()).isEqualTo("john@example.com");
    assertThat(result.firstName()).isEqualTo("John");
    assertThat(result.lastName()).isEqualTo("Doe");
    assertThat(result.id()).isNotNull();

    verify(passwordHasher).hash("SecurePass123!");

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.email().value()).isEqualTo("john@example.com");
    assertThat(savedUser.username().value()).isEqualTo("john_doe");
    assertThat(savedUser.firstName()).isEqualTo("John");
    assertThat(savedUser.lastName()).isEqualTo("Doe");
    assertThat(savedUser.passwordHash()).isEqualTo(hashedPassword);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventPublisher).publishAll(eventsCaptor.capture());

    List<DomainEvent> publishedEvents = eventsCaptor.getValue();
    assertThat(publishedEvents).hasSize(1);
    assertThat(publishedEvents.get(0)).isInstanceOf(UserCreatedEvent.class);
  }

  @Test
  void shouldThrowException_WhenEmailAlreadyExists() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(DuplicateEmailException.class)
        .hasMessage(IdentityExceptionCodes.DUPLICATE_EMAIL.name());
    verify(userRepository).existsByEmail(any(Email.class));
    verifyNoMoreInteractions(userRepository, passwordHasher, eventPublisher);
  }

  @Test
  void shouldThrowException_WhenUsernameAlreadyExists() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(DuplicateUsernameException.class)
        .hasMessage(IdentityExceptionCodes.DUPLICATE_USERNAME.name());
    verify(userRepository).existsByEmail(any(Email.class));
    verify(userRepository).existsByUsername(any(Username.class));
    verifyNoMoreInteractions(userRepository, passwordHasher, eventPublisher);
  }

  @Test
  void shouldThrowException_WhenPasswordPolicyValidationFails() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "weak");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);

    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(InvalidPasswordException.class)
        .satisfies(ex -> {
          InvalidPasswordException invalid = (InvalidPasswordException) ex;
          org.assertj.core.api.Assertions.assertThat(invalid.violation()).isEqualTo(PasswordViolation.TOO_SHORT);
        });
  }

  @Test
  void shouldCreateUniqueUserIds_WhenRegisteringMultipleUsers() {
    RegisterUserCommand command1 = aCommand("john_doe", "john@example.com", "SecurePass123!");
    RegisterUserCommand command2 = new RegisterUserCommand("Jane", "Smith", new Username("janesmith"),
        new Email("jane@example.com"), "SecurePass456!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn("hashed");

    UserResult result1 = handler.handle(command1).orElseThrow();
    UserResult result2 = handler.handle(command2).orElseThrow();

    assertThat(result1.id()).isNotEqualTo(result2.id());
  }

  @Test
  void shouldValidatePasswordBeforeHashing() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn("hashed");

    handler.handle(command);

    verify(passwordHasher).hash("SecurePass123!");
  }

  private RegisterUserCommand aCommand(String username, String email, String password) {
    return new RegisterUserCommand("John", "Doe", new Username(username), new Email(email), password);
  }
}
