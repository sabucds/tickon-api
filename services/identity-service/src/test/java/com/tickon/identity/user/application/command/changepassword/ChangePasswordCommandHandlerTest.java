package com.tickon.identity.user.application.command.changepassword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.commands.CommandResult;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.contracts.user.commands.ChangePasswordCommand;
import com.tickon.identity.shared.kernel.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.exceptions.InvalidPasswordException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangePasswordCommandHandlerTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordHasher passwordHasher;

  private ChangePasswordCommandHandler handler;
  private PasswordStrengthPolicy passwordPolicy;

  private User createTestUser(UserId userId, PasswordHash passwordHash) {
    return User.restore(userId, Email.from("test@example.com"), Username.from("testuser"), "Test", "User", passwordHash,
        UserStatus.ACTIVE);
  }

  @BeforeEach
  void setUp() {
    passwordPolicy = new PasswordStrengthPolicy();
    handler = new ChangePasswordCommandHandler(userRepository, passwordHasher, passwordPolicy);
  }

  @Test
  void shouldChangePasswordSuccessfully() {
    // Arrange
    UserId userId = new UserId(UUID.randomUUID());
    String plainPassword = "NewSecure123!";
    PasswordHash oldPasswordHash = new PasswordHash("oldHashedPassword");
    PasswordHash newPasswordHash = new PasswordHash("newHashedPassword");
    User user = createTestUser(userId, oldPasswordHash);

    ChangePasswordCommand command = new ChangePasswordCommand(userId, plainPassword);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordHasher.hash(plainPassword)).thenReturn(newPasswordHash);

    // Act
    CommandResult<Void> result = handler.handle(command);

    // Assert
    assertThat(result.isSuccess()).isTrue();
    verify(passwordHasher).hash(plainPassword);
    verify(userRepository).findById(userId);
    verify(userRepository)
        .save(argThat(savedUser -> savedUser.id().equals(userId) && savedUser.passwordHash().equals(newPasswordHash)));
  }

  @Test
  void shouldThrowException_WhenUserNotFound() {
    // Arrange
    UserId userId = new UserId(UUID.randomUUID());
    String plainPassword = "NewSecure123!";
    PasswordHash newPasswordHash = new PasswordHash("newHashedPassword");
    ChangePasswordCommand command = new ChangePasswordCommand(userId, plainPassword);

    when(passwordHasher.hash(plainPassword)).thenReturn(newPasswordHash);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("User not found");

    verify(passwordHasher).hash(plainPassword);
    verify(userRepository).findById(userId);
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldThrowException_WhenRepositorySaveFails() {
    // Arrange
    UserId userId = new UserId(UUID.randomUUID());
    String plainPassword = "NewSecure123!";
    PasswordHash oldPasswordHash = new PasswordHash("oldHashedPassword");
    PasswordHash newPasswordHash = new PasswordHash("newHashedPassword");
    User user = createTestUser(userId, oldPasswordHash);

    ChangePasswordCommand command = new ChangePasswordCommand(userId, plainPassword);

    when(passwordHasher.hash(plainPassword)).thenReturn(newPasswordHash);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

    // Act & Assert
    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(RuntimeException.class).hasMessage("Database error");

    verify(passwordHasher).hash(plainPassword);
    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowException_WhenPasswordValidationFails() {
    // Arrange
    UserId userId = new UserId(UUID.randomUUID());
    String weakPassword = "weak";
    ChangePasswordCommand command = new ChangePasswordCommand(userId, weakPassword);

    // Act & Assert
    assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(InvalidPasswordException.class)
        .hasMessageContaining("Password must be at least");

    verify(passwordHasher, never()).hash(any());
    verify(userRepository, never()).findById(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldGetCorrectCommandClass() {
    assertThat(handler.getCommandClass()).isEqualTo(ChangePasswordCommand.class);
  }
}
