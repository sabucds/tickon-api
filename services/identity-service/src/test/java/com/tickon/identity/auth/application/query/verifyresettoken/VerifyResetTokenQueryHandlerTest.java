package com.tickon.identity.auth.application.query.verifyresettoken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.identity.auth.application.ports.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
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
class VerifyResetTokenQueryHandlerTest {

  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;

  private VerifyResetTokenQueryHandler handler;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    handler = new VerifyResetTokenQueryHandler(resetTokenRepository, resetTokenHasher, fixedClock);
  }

  @Test
  void shouldReturnValid_WhenTokenExistsAndNotExpiredOrUsed() {
    String plainToken = "valid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UUID.randomUUID(),
        "user@example.com", Duration.ofHours(1), fixedInstant.minusSeconds(30), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));

    VerifyResetTokenResult result = handler.handle(new VerifyResetTokenQuery(plainToken)).orElseThrow();

    assertThat(result.valid()).isTrue();
  }

  @Test
  void shouldReturnInvalid_WhenTokenDoesNotExist() {
    String plainToken = "nonexistent-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    VerifyResetTokenResult result = handler.handle(new VerifyResetTokenQuery(plainToken)).orElseThrow();

    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsExpired() {
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UUID.randomUUID(),
        "user@example.com", Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)), "test-plain-token");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    VerifyResetTokenResult result = handler.handle(new VerifyResetTokenQuery(plainToken)).orElseThrow();

    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsUsed() {
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, UUID.randomUUID(),
        "user@example.com", fixedInstant.plus(Duration.ofHours(1)), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    VerifyResetTokenResult result = handler.handle(new VerifyResetTokenQuery(plainToken)).orElseThrow();

    assertThat(result.valid()).isFalse();
  }
}
