package com.tickon.identity.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.events.PasswordResetCompletedEvent;
import com.tickon.identity.auth.domain.events.PasswordResetRequestedEvent;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T10:00:00Z");
  private static final Duration ONE_HOUR = Duration.ofHours(1);
  private static final UserId USER_ID = UserId.generate();
  private static final Email EMAIL = Email.from("user@example.com");

  @Test
  void shouldCreateTokenWithValidParameters() {
    ResetTokenId tokenId = ResetTokenId.generate();
    ResetTokenHash tokenHash = ResetTokenHash.from("sample-hash");

    PasswordResetToken token = PasswordResetToken.create(tokenId, tokenHash, USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT,
        "test-plain-token");

    assertThat(token.id()).isEqualTo(tokenId);
    assertThat(token.tokenHash()).isEqualTo(tokenHash);
    assertThat(token.userId()).isEqualTo(USER_ID);
    assertThat(token.email()).isEqualTo(EMAIL);
    assertThat(token.absoluteExpiresAt()).isEqualTo(FIXED_INSTANT.plus(ONE_HOUR));
    assertThat(token.usedAt()).isNull();
    assertThat(token.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(token.isUsed()).isFalse();
  }

  @Test
  void shouldRegisterPasswordResetRequestedEvent_WhenCreatingToken() {
    ResetTokenId tokenId = ResetTokenId.generate();
    PasswordResetToken token = PasswordResetToken.create(tokenId, ResetTokenHash.from("sample-hash"), USER_ID, EMAIL,
        ONE_HOUR, FIXED_INSTANT, "test-plain-token");

    List<DomainEvent> events = token.domainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(PasswordResetRequestedEvent.class);

    PasswordResetRequestedEvent event = (PasswordResetRequestedEvent) events.get(0);
    assertThat(event.userId()).isEqualTo(USER_ID);
    assertThat(event.email()).isEqualTo(EMAIL);
    assertThat(event.occurredOn()).isNotNull();
  }

  @Test
  void shouldRestoreToken() {
    ResetTokenId tokenId = ResetTokenId.generate();
    Instant expiresAt = FIXED_INSTANT.plus(ONE_HOUR);

    PasswordResetToken token = PasswordResetToken.restore(tokenId, ResetTokenHash.from("sample-hash"), USER_ID, EMAIL,
        expiresAt, null);

    assertThat(token.id()).isEqualTo(tokenId);
    assertThat(token.tokenHash().value()).isEqualTo("sample-hash");
    assertThat(token.userId()).isEqualTo(USER_ID);
    assertThat(token.email()).isEqualTo(EMAIL);
    assertThat(token.absoluteExpiresAt()).isEqualTo(expiresAt);
    assertThat(token.usedAt()).isNull();
  }

  @Test
  void shouldNotRegisterEvents_WhenRestoringToken() {
    Instant expiresAt = FIXED_INSTANT.plus(ONE_HOUR);
    PasswordResetToken token = PasswordResetToken.restore(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, expiresAt, null);

    assertThat(token.domainEvents()).isEmpty();
  }

  @Test
  void shouldMarkAsUsed() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");
    assertThat(token.usedAt()).isNull();
    assertThat(token.isUsed()).isFalse();

    Instant usedTime = FIXED_INSTANT.plusSeconds(30);
    token.markAsUsed(usedTime);

    assertThat(token.usedAt()).isEqualTo(usedTime);
    assertThat(token.isUsed()).isTrue();
  }

  @Test
  void shouldRegisterPasswordResetCompletedEvent_WhenMarkingAsUsed() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");
    token.clearEvents();

    Instant usedTime = FIXED_INSTANT.plusSeconds(30);
    token.markAsUsed(usedTime);

    List<DomainEvent> events = token.domainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(PasswordResetCompletedEvent.class);

    PasswordResetCompletedEvent event = (PasswordResetCompletedEvent) events.get(0);
    assertThat(event.userId()).isEqualTo(USER_ID);
    assertThat(event.resetTokenId()).isEqualTo(token.id());
    assertThat(event.occurredOn()).isNotNull();
  }

  @Test
  void shouldThrow_WhenMarkingAsUsedTwice() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");

    token.markAsUsed(FIXED_INSTANT.plusSeconds(30));

    assertThatThrownBy(() -> token.markAsUsed(FIXED_INSTANT.plusSeconds(60))).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already been used");
  }

  @Test
  void shouldThrow_WhenMarkingExpiredTokenAsUsed() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, Duration.ofSeconds(30), FIXED_INSTANT, "test-plain-token");

    Instant afterExpiration = FIXED_INSTANT.plusSeconds(60);
    assertThat(token.isExpired(afterExpiration)).isTrue();

    assertThatThrownBy(() -> token.markAsUsed(afterExpiration)).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("expired");
  }

  @Test
  void shouldIdentifyExpiredToken() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, Duration.ofSeconds(30), FIXED_INSTANT, "test-plain-token");

    assertThat(token.isExpired(FIXED_INSTANT.plusSeconds(15))).isFalse();
    assertThat(token.isExpired(FIXED_INSTANT.plusSeconds(30))).isTrue(); // Exactly at expiration
    assertThat(token.isExpired(FIXED_INSTANT.plusSeconds(45))).isTrue(); // After expiration
  }

  @Test
  void shouldIdentifyUsedToken() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");

    assertThat(token.isUsed()).isFalse();

    token.markAsUsed(FIXED_INSTANT.plusSeconds(30));
    assertThat(token.isUsed()).isTrue();
  }

  @Test
  void shouldThrow_WhenCreatingTokenWithNullNow() {
    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, null, "test-plain-token")).isInstanceOf(NullPointerException.class).hasMessage("now");
  }

  @Test
  void shouldThrow_WhenCreatingTokenWithNonPositiveDuration() {
    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, Duration.ofSeconds(0), FIXED_INSTANT, "test-plain-token"))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("duration must be positive");

    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, Duration.ofSeconds(-10), FIXED_INSTANT, "test-plain-token"))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("duration must be positive");
  }

  @Test
  void shouldThrow_WhenCreatingTokenWithNullParameters() {
    assertThatThrownBy(() -> PasswordResetToken.create(null, ResetTokenHash.from("hash"), USER_ID, EMAIL, ONE_HOUR,
        FIXED_INSTANT, "test-plain-token")).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), null, USER_ID, EMAIL, ONE_HOUR,
        FIXED_INSTANT, "test-plain-token")).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("hash"), null,
        EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token")).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(() -> PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("hash"), USER_ID,
        null, ONE_HOUR, FIXED_INSTANT, "test-plain-token")).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrow_WhenMarkingAsUsedWithNullTime() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");

    assertThatThrownBy(() -> token.markAsUsed(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrow_WhenCheckingExpirationWithNullTime() {
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), ResetTokenHash.from("sample-hash"),
        USER_ID, EMAIL, ONE_HOUR, FIXED_INSTANT, "test-plain-token");

    assertThatThrownBy(() -> token.isExpired(null)).isInstanceOf(NullPointerException.class);
  }
}
