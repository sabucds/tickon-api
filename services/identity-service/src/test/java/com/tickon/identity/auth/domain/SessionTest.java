package com.tickon.identity.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.events.SessionCreatedEvent;
import com.tickon.identity.auth.domain.events.SessionRevokedEvent;
import com.tickon.identity.auth.domain.exceptions.SessionExpiredException;
import com.tickon.identity.auth.domain.exceptions.SessionRevokedException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T10:00:00Z");
  private static final Duration DURATION_30_DAYS = java.time.Duration.ofDays(30);
  private static final UserId USER_ID = UserId.generate();

  @Test
  void shouldCreateSessionWithValidParameters() {
    Session session = Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID, "device-123",
        FamilyId.generate(), null, DURATION_30_DAYS, FIXED_INSTANT);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.absoluteExpiresAt()).isEqualTo(FIXED_INSTANT.plus(DURATION_30_DAYS));
  }

  @Test
  void shouldRegisterSessionCreatedEvent_WhenCreatingSession() {
    SessionId sessionId = SessionId.generate();
    Session session = Session.create(sessionId, RefreshTokenHash.from("sample-hash"), USER_ID, "device-123",
        FamilyId.generate(), null, DURATION_30_DAYS, FIXED_INSTANT);

    List<DomainEvent> events = session.domainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(SessionCreatedEvent.class);

    SessionCreatedEvent event = (SessionCreatedEvent) events.get(0);
    assertThat(event.sessionId()).isEqualTo(sessionId);
    assertThat(event.userId()).isEqualTo(USER_ID);
    assertThat(event.occurredOn()).isNotNull();
  }

  @Test
  void shouldRestoreSession() {
    Instant absoluteExpiresAt = FIXED_INSTANT.plus(DURATION_30_DAYS);
    Session session = Session.restore(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID, "device-123",
        FamilyId.generate(), null, absoluteExpiresAt, null, null);
    assertThat(session.id()).isNotNull();
    assertThat(session.refreshTokenHash().value()).isEqualTo("sample-hash");
    assertThat(session.deviceId()).isEqualTo("device-123");
    assertThat(session.isExpired(FIXED_INSTANT)).isFalse();
    assertThat(session.familyId()).isNotNull();
    assertThat(session.rotatedFromSessionId()).isNull();
    assertThat(session.absoluteExpiresAt()).isEqualTo(absoluteExpiresAt);
  }

  @Test
  void shouldNotRegisterEvents_WhenRestoringSession() {
    Instant absoluteExpiresAt = FIXED_INSTANT.plus(DURATION_30_DAYS);
    Session session = Session.restore(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID, "device-123",
        FamilyId.generate(), null, absoluteExpiresAt, null, null);

    assertThat(session.domainEvents()).isEmpty();
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
  void shouldRegisterSessionRevokedEvent_WhenRevokingSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    session.clearEvents();

    session.revoke(FIXED_INSTANT.plusSeconds(10), RevokeReason.USER_LOGOUT);

    List<DomainEvent> events = session.domainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(SessionRevokedEvent.class);

    SessionRevokedEvent event = (SessionRevokedEvent) events.get(0);
    assertThat(event.sessionId()).isEqualTo(session.id());
    assertThat(event.reason()).isEqualTo(RevokeReason.USER_LOGOUT);
    assertThat(event.occurredOn()).isNotNull();
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

  @Test
  void shouldRotateFromAnotherSession() {
    Session originalSession = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    Session rotatedSession = originalSession.rotateTo(FIXED_INSTANT.plusSeconds(10), RefreshTokenHash.from("new-hash"),
        SessionId.generate());

    assertThat(rotatedSession.id()).isNotEqualTo(originalSession.id());
    assertThat(rotatedSession.refreshTokenHash().value()).isEqualTo("new-hash");
    assertThat(rotatedSession.userId()).isEqualTo(originalSession.userId());
    assertThat(rotatedSession.deviceId()).isEqualTo(originalSession.deviceId());
    assertThat(rotatedSession.familyId()).isEqualTo(originalSession.familyId());
    assertThat(rotatedSession.rotatedFromSessionId()).isEqualTo(originalSession.id());
    assertThat(rotatedSession.absoluteExpiresAt()).isEqualTo(originalSession.absoluteExpiresAt());
  }

  @Test
  void shouldRegisterEvents_WhenRotatingSession() {
    Session originalSession = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    originalSession.clearEvents();

    SessionId newSessionId = SessionId.generate();
    Session rotatedSession = originalSession.rotateTo(FIXED_INSTANT.plusSeconds(10), RefreshTokenHash.from("new-hash"),
        newSessionId);

    List<DomainEvent> originalEvents = originalSession.domainEvents();
    assertThat(originalEvents).hasSize(1);
    assertThat(originalEvents.get(0)).isInstanceOf(SessionRevokedEvent.class);
    SessionRevokedEvent revokedEvent = (SessionRevokedEvent) originalEvents.get(0);
    assertThat(revokedEvent.sessionId()).isEqualTo(originalSession.id());
    assertThat(revokedEvent.reason()).isEqualTo(RevokeReason.SESSION_ROTATED);

    List<DomainEvent> newEvents = rotatedSession.domainEvents();
    assertThat(newEvents).hasSize(1);
    assertThat(newEvents.get(0)).isInstanceOf(SessionCreatedEvent.class);
    SessionCreatedEvent createdEvent = (SessionCreatedEvent) newEvents.get(0);
    assertThat(createdEvent.sessionId()).isEqualTo(newSessionId);
    assertThat(createdEvent.userId()).isEqualTo(originalSession.userId());
  }

  @Test
  void shouldThrow_WhenRotatingAnExpiredSession() {
    Session originalSession = AuthTestFixtures.aSession(FIXED_INSTANT, Duration.ofSeconds(10));
    Instant rotateTime = FIXED_INSTANT.plusSeconds(15);
    assertThatThrownBy(
        () -> originalSession.rotateTo(rotateTime, RefreshTokenHash.from("new-hash"), SessionId.generate()))
        .isInstanceOf(SessionExpiredException.class).hasMessage("Session is already expired");
  }

  @Test
  void shouldIdentifyRevokedSession() {
    Session session = AuthTestFixtures.aSession(FIXED_INSTANT, DURATION_30_DAYS);
    assertThat(session.isRevoked()).isFalse();

    session.revoke(FIXED_INSTANT.plusSeconds(10), RevokeReason.USER_LOGOUT);
    assertThat(session.isRevoked()).isTrue();
  }

  @Test
  void shouldThrow_WhenRestoringSessionWithInconsistentRevocationData() {
    Instant absoluteExpiresAt = FIXED_INSTANT.plus(DURATION_30_DAYS);
    assertThatThrownBy(() -> Session.restore(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID,
        "device-123", FamilyId.generate(), null, absoluteExpiresAt, null, RevokeReason.USER_LOGOUT))
        .isInstanceOf(IllegalStateException.class).hasMessage("revokedAt and revokeReason are inconsistent");

    assertThatThrownBy(() -> Session.restore(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID,
        "device-123", FamilyId.generate(), null, absoluteExpiresAt, FIXED_INSTANT, null))
        .isInstanceOf(IllegalStateException.class).hasMessage("revokedAt and revokeReason are inconsistent");
  }

  @Test
  void shouldThrow_WhenCreatingSessionWithNullNow() {
    assertThatThrownBy(() -> Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID,
        "device-123", FamilyId.generate(), null, DURATION_30_DAYS, null)).isInstanceOf(NullPointerException.class)
        .hasMessage("now");
  }

  @Test
  void shouldThrow_WhenCreatingSessionWithNonPositiveDuration() {
    assertThatThrownBy(() -> Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID,
        "device-123", FamilyId.generate(), null, Duration.ofSeconds(0), FIXED_INSTANT))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("sessionDuration must be positive");
    assertThatThrownBy(() -> Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID,
        "device-123", FamilyId.generate(), null, Duration.ofSeconds(-10), FIXED_INSTANT))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("sessionDuration must be positive");
  }

  @Test
  void shouldThrow_WhenCreatingSessionWithBlankDeviceId() {
    assertThatThrownBy(() -> Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), USER_ID, "",
        FamilyId.generate(), null, DURATION_30_DAYS, FIXED_INSTANT)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("deviceId cannot be blank");
  }
}