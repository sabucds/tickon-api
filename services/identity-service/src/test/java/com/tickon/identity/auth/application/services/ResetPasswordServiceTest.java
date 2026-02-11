package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.exceptions.CommandExecutionException;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.dto.ResetPasswordCommand;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.exceptions.InvalidResetTokenException;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.shared.contracts.commands.ChangePasswordCommand;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;
  @Mock
  private CommandBus commandBus;
  @Mock
  private DomainEventPublisher eventPublisher;

  private ResetPasswordService service;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    service = new ResetPasswordService(resetTokenRepository, resetTokenHasher, fixedClock, commandBus, eventPublisher);
  }

  @Test
  void shouldResetPassword_WhenTokenIsValidAndCommandSucceeds() {
    // Arrange
    String plainToken = "valid-token";
    String newPassword = "NewSecure123!";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed-token");
    UserId userId = UserId.generate();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    when(commandBus.execute(any(ChangePasswordCommand.class))).thenReturn(new CommandResult.Success<>(null));

    // Act
    service.resetPassword(new ResetPasswordCommand(plainToken, newPassword));

    // Assert
    assertThat(token.isUsed()).isTrue();
    assertThat(token.usedAt()).isEqualTo(fixedInstant);
    verify(commandBus).execute(any(ChangePasswordCommand.class));
    verify(resetTokenRepository).save(token);
  }

  @Test
  void shouldThrow_WhenCommandFails() {
    // Arrange
    String plainToken = "valid-token";
    String newPassword = "NewSecure123!";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed-token");
    UserId userId = UserId.generate();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    doThrow(new CommandExecutionException(ChangePasswordCommand.class, new RuntimeException("Database error")))
        .when(commandBus).execute(any(ChangePasswordCommand.class));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, newPassword)))
        .isInstanceOf(CommandExecutionException.class)
        .hasMessageContaining("Error executing command: ChangePasswordCommand");

    assertThat(token.isUsed()).isFalse();
    verify(resetTokenRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenTokenDoesNotExist() {
    // Arrange
    String plainToken = "invalid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);

    verify(resetTokenRepository, never()).save(any());
    verify(commandBus, never()).execute(any());
  }

  @Test
  void shouldThrow_WhenTokenIsExpired() {
    // Arrange
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    // Token created 2 hours ago, expired after 1 hour
    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)),
        "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);
  }

  @Test
  void shouldThrow_WhenTokenAlreadyUsed() {
    // Arrange
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), fixedInstant.plus(Duration.ofHours(1)), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);
  }

  @Test
  void shouldThrow_WhenPasswordValidationFailsInUserModule() {
    // Arrange
    String plainToken = "valid-token";
    String weakPassword = "weak";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    doThrow(
        new CommandExecutionException(ChangePasswordCommand.class, new IllegalArgumentException("Password too weak")))
        .when(commandBus).execute(any(ChangePasswordCommand.class));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, weakPassword)))
        .isInstanceOf(CommandExecutionException.class)
        .hasMessageContaining("Error executing command: ChangePasswordCommand");

    assertThat(token.isUsed()).isFalse();
    verify(resetTokenRepository, never()).save(any());
  }
}
