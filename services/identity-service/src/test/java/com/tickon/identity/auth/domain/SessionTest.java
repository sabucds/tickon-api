package com.tickon.identity.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

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
        "device-123", FamilyId.generate(), null, FIXED_INSTANT, DURATION_30_DAYS);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.expiresAt()).isEqualTo(FIXED_INSTANT.plus(DURATION_30_DAYS));
  }

  @Test
  void shouldRestoreSessionFromPersistence() {
    Instant expiresAt = FIXED_INSTANT.plus(DURATION_30_DAYS);
    Session session = Session.fromPersistence(SessionId.generate(), RefreshTokenHash.from("sample-hash"),
        UserId.generate(), "device-123", FamilyId.generate(), null, expiresAt, null, null);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.expiresAt()).isEqualTo(expiresAt);
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
  void shouldNotRevokeAlreadyRevokedSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    session.revoke(FIXED_INSTANT.plusSeconds(10), RevokeReason.USER_LOGOUT);
    Instant firstRevokedAt = session.revokedAt();
    RevokeReason firstRevokeReason = session.revokeReason();
    session.revoke(FIXED_INSTANT.plusSeconds(20), RevokeReason.TOKEN_COMPROMISED);
    assertThat(session.revokedAt()).isEqualTo(firstRevokedAt);
    assertThat(session.revokeReason()).isEqualTo(firstRevokeReason);
  }

  @Test
  void shouldIdentifyExpiredSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, Duration.ofSeconds(10));
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(5))).isFalse();
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(10))).isTrue();
    assertThat(session.isExpired(FIXED_INSTANT.plusSeconds(15))).isTrue();
  }

}