package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
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
class VerifyResetTokenServiceTest {

  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;

  private VerifyResetTokenService service;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    service = new VerifyResetTokenService(resetTokenRepository, resetTokenHasher, fixedClock);
  }

  @Test
  void shouldReturnValid_WhenTokenExistsAndNotExpiredOrUsed() {
    // Arrange
    String plainToken = "valid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isTrue();
  }

  @Test
  void shouldReturnInvalid_WhenTokenDoesNotExist() {
    // Arrange
    String plainToken = "nonexistent-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsExpired() {
    // Arrange
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)),
        "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsUsed() {
    // Arrange
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), fixedInstant.plus(Duration.ofHours(1)), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }
}
