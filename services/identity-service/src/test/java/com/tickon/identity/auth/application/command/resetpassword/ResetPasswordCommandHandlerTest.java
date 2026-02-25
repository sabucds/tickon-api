package com.tickon.identity.auth.application.command.resetpassword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.commands.exceptions.CommandExecutionException;
import com.tickon.identity.auth.application.ports.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.exceptions.InvalidResetTokenException;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.contracts.user.commands.ChangePasswordCommand;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResetPasswordCommandHandlerTest {

  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;
  @Mock
  private CommandBus commandBus;
  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private ResetPasswordCommandHandler handler;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    handler = new ResetPasswordCommandHandler(resetTokenRepository, resetTokenHasher, fixedClock, commandBus,
        eventPublisher, metrics);
  }

  @Test
  void shouldResetPassword_WhenTokenIsValidAndCommandSucceeds() {
    String plainToken = "valid-token";
    String newPassword = "NewSecure123!";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed-token");
    UUID userId = UUID.randomUUID();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId, "user@example.com",
        Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    when(commandBus.execute(any(ChangePasswordCommand.class))).thenReturn(new CommandResult.Success<>(null));

    handler.handle(new ResetPasswordCommand(plainToken, newPassword));

    assertThat(token.isUsed()).isTrue();
    assertThat(token.usedAt()).isEqualTo(fixedInstant);
    verify(commandBus).execute(any(ChangePasswordCommand.class));
    verify(resetTokenRepository).save(token);
  }

  @Test
  void shouldThrow_WhenCommandFails() {
    String plainToken = "valid-token";
    String newPassword = "NewSecure123!";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed-token");
    UUID userId = UUID.randomUUID();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId, "user@example.com",
        Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    doThrow(new CommandExecutionException(ChangePasswordCommand.class, new RuntimeException("Database error")))
        .when(commandBus).execute(any(ChangePasswordCommand.class));

    assertThatThrownBy(() -> handler.handle(new ResetPasswordCommand(plainToken, newPassword)))
        .isInstanceOf(CommandExecutionException.class)
        .hasMessageContaining("Error executing command: ChangePasswordCommand");

    assertThat(token.isUsed()).isFalse();
    verify(resetTokenRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenTokenDoesNotExist() {
    String plainToken = "invalid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);

    verify(resetTokenRepository, never()).save(any());
    verify(commandBus, never()).execute(any());
  }

  @Test
  void shouldThrow_WhenTokenIsExpired() {
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UUID userId = UUID.randomUUID();

    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        "user@example.com", Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    assertThatThrownBy(() -> handler.handle(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);
  }

  @Test
  void shouldThrow_WhenTokenAlreadyUsed() {
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UUID userId = UUID.randomUUID();

    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, userId,
        "user@example.com", fixedInstant.plus(Duration.ofHours(1)), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    assertThatThrownBy(() -> handler.handle(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);
  }
}
