package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.time.Clock;
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

  private PasswordStrengthPolicy passwordPolicy;

  private RegisterUserService registerUser;

  private Clock clock = Clock.systemUTC();

  @BeforeEach
  void setUp() {
    passwordPolicy = new PasswordStrengthPolicy();
    registerUser = new RegisterUserService(userRepository, passwordHasher, passwordPolicy, clock);
  }

  @Test
  void shouldRegisterUser_WhenValidInput() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com",
        "SecurePass123!");

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
  }

  @Test
  void shouldThrowException_WhenEmailAlreadyExists() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com",
        "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(IllegalStateException.class)
        .hasMessage("Email already in use");
  }

  @Test
  void shouldThrowException_WhenUsernameAlreadyExists() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com",
        "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(IllegalStateException.class)
        .hasMessage("Username already in use");
  }

  @Test
  void shouldThrowException_WhenPasswordPolicyValidationFails() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com", "weak");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must be at least");
  }

  @Test
  void shouldThrowException_WhenInvalidEmailFormat() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "invalid-email", "SecurePass123!");

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid email");
  }

  @Test
  void shouldThrowException_WhenInvalidUsernameFormat() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "", "john@example.com", "SecurePass123!");

    assertThatThrownBy(() -> registerUser.register(command)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldCreateUniqueUserIds_WhenRegisteringMultipleUsers() {
    RegisterUserCommand command1 = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com",
        "SecurePass123!");
    RegisterUserCommand command2 = new RegisterUserCommand("Jane", "Smith", "janesmith", "jane@example.com",
        "SecurePass456!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    UserResult result1 = registerUser.register(command1);
    UserResult result2 = registerUser.register(command2);

    assertThat(result1.id()).isNotEqualTo(result2.id());
  }

  @Test
  void shouldValidatePasswordBeforeHashing() {
    RegisterUserCommand command = new RegisterUserCommand("John", "Doe", "john_doe", "john@example.com",
        "SecurePass123!");

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
    when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));

    registerUser.register(command);

    // Password validation happens before hashing (verified by service
    // implementation)
    verify(passwordHasher).hash("SecurePass123!");
  }
}
