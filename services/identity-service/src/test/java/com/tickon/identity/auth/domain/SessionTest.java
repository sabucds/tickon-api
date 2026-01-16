package com.tickon.identity.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.identity.auth.domain.exceptions.SessionRevokedException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T10:00:00Z");
  private static final Duration DURATION_30_DAYS = java.time.Duration.ofDays(30);

  @Test
  void shouldCreateSessionWithValidParameters() {
    Session session = Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), UserId.generate(),
        "device-123", FamilyId.generate(), null, DURATION_30_DAYS, FIXED_INSTANT);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.absoluteExpiresAt()).isEqualTo(FIXED_INSTANT.plus(DURATION_30_DAYS));
  }

  @Test
  void shouldRestoreSession() {
    Instant absoluteExpiresAt = FIXED_INSTANT.plus(DURATION_30_DAYS);
    Session session = Session.restore(SessionId.generate(), RefreshTokenHash.from("sample-hash"), UserId.generate(),
        "device-123", FamilyId.generate(), null, absoluteExpiresAt, null, null);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.absoluteExpiresAt()).isEqualTo(absoluteExpiresAt);
  }

  @Test
  void shouldRevokeSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    assertThat(session.revokedAt()).isNull();
    assertThat(session.revokeReason()).isNull();

    session.revoke(FIXED_INSTANT.plusSeconds(10), RevokeReason.USER_LOGOUT);
    assertThat(session.revokedAt()).isEqualTo(FIXED_INSTANT.plusSeconds(10));
    assertThat(session.revokeReason()).isEqualTo(RevokeReason.USER_LOGOUT);
  }

  @Test
  void shouldThrow_WhenRevokingAnAlreadyRevokedSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    session.revoke(FIXED_INSTANT.plusSeconds(10), RevokeReason.USER_LOGOUT);
    Instant revokeTime = FIXED_INSTANT.plusSeconds(20);
    assertThatThrownBy(() -> session.revoke(revokeTime, RevokeReason.TOKEN_COMPROMISED))
        .isInstanceOf(SessionRevokedException.class).hasMessage("Session is already revoked for a different reason");

  }

  @Test
  void shouldIdentifyExpiredSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, Duration.ofSeconds(10));
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(5))).isFalse();
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(10))).isTrue();
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(15))).isTrue();
  }

}