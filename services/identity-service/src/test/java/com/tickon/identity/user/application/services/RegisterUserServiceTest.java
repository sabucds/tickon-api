package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.events.UserCreatedEvent;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.exceptions.InvalidPasswordException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    PasswordHash hashedPassword = new PasswordHash("hashed-password");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash("SecurePass123!")).thenReturn(hashedPassword);

    UserResult result = registerUser.register(command);

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

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(DuplicateEmailException.class)
        .hasMessage("Email already in use: john@example.com");
    verify(userRepository).existsByEmail(any(Email.class));
    verifyNoMoreInteractions(userRepository, passwordHasher, eventPublisher);
  }

  @Test
  void shouldThrowException_WhenUsernameAlreadyExists() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(DuplicateUsernameException.class)
        .hasMessage("Username already in use: john_doe");
    verify(userRepository).existsByEmail(any(Email.class));
    verify(userRepository).existsByUsername(any(Username.class));
    verifyNoMoreInteractions(userRepository, passwordHasher, eventPublisher);
  }

  @Test
  void shouldThrowException_WhenPasswordPolicyValidationFails() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "weak");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(InvalidPasswordException.class)
        .hasMessageContaining("Password must be at least");
  }

  @Test
  void shouldCreateUniqueUserIds_WhenRegisteringMultipleUsers() {
    RegisterUserCommand command1 = aCommand("john_doe", "john@example.com", "SecurePass123!");
    RegisterUserCommand command2 = new RegisterUserCommand("Jane", "Smith", new Username("janesmith"),
        new Email("jane@example.com"), "SecurePass456!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    UserResult result1 = registerUser.register(command1);
    UserResult result2 = registerUser.register(command2);

    assertThat(result1.id()).isNotEqualTo(result2.id());
  }

  @Test
  void shouldValidatePasswordBeforeHashing() {
    RegisterUserCommand command = aCommand("john_doe", "john@example.com", "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    registerUser.register(command);

    verify(passwordHasher).hash("SecurePass123!");
  }

  private RegisterUserCommand aCommand(String username, String email, String password) {
    return new RegisterUserCommand("John", "Doe", new Username(username), new Email(email), password);
  }
}
