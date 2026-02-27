package com.tickon.identity.auth.domain;

import com.tickon.common.domain.AggregateRoot;
import com.tickon.identity.auth.domain.events.SessionCreatedEvent;
import com.tickon.identity.auth.domain.events.SessionRevokedEvent;
import com.tickon.identity.auth.domain.exceptions.SessionExpiredException;
import com.tickon.identity.auth.domain.exceptions.SessionRevokedException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Session extends AggregateRoot {

  private final SessionId id;
  private final RefreshTokenHash refreshTokenHash;
  private final UUID userId;
  private final String deviceId;
  private final FamilyId familyId;
  private final SessionId rotatedFromSessionId;

  private final Instant absoluteExpiresAt;

  private Instant revokedAt;
  private RevokeReason revokeReason;

  private Session(SessionId id, RefreshTokenHash refreshTokenHash, UUID userId, String deviceId, FamilyId familyId,
      SessionId rotatedFromSessionId, Instant absoluteExpiresAt, Instant revokedAt, RevokeReason revokeReason) {
    this.id = Objects.requireNonNull(id, "id");
    this.refreshTokenHash = Objects.requireNonNull(refreshTokenHash, "refreshTokenHash");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.deviceId = Objects.requireNonNull(deviceId, "deviceId");
    this.familyId = Objects.requireNonNull(familyId, "familyId");
    this.rotatedFromSessionId = rotatedFromSessionId;
    this.absoluteExpiresAt = Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt");
    this.revokedAt = revokedAt;
    this.revokeReason = revokeReason;

    if (deviceId.isBlank()) {
      throw new IllegalArgumentException("deviceId cannot be blank");
    }
    this.validateRevocationConsistency();
  }

  public static Session create(SessionId id, RefreshTokenHash refreshTokenHash, UUID userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Duration sessionDuration, Instant now) {
    Objects.requireNonNull(sessionDuration, "sessionDuration");
    Objects.requireNonNull(now, "now");
    if (sessionDuration.isZero() || sessionDuration.isNegative()) {
      throw new IllegalArgumentException("sessionDuration must be positive");
    }
    Instant absoluteExpiresAt = now.plus(sessionDuration);
    Session session = new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId,
        absoluteExpiresAt, null, null);
    session.registerEvent(new SessionCreatedEvent(id, userId));
    return session;
  }

  public static Session restore(SessionId id, RefreshTokenHash refreshTokenHash, UUID userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Instant absoluteExpiresAt, Instant revokedAt,
      RevokeReason revokeReason) {
    return new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId, absoluteExpiresAt,
        revokedAt, revokeReason);
  }

  public Session rotateTo(Instant now, RefreshTokenHash newRefreshTokenHash, SessionId newSessionId) {
    if (isExpired(now)) {
      throw new SessionExpiredException();
    }
    revoke(now, RevokeReason.SESSION_ROTATED);
    Session newSession = new Session(newSessionId, newRefreshTokenHash, userId, deviceId, familyId, id,
        absoluteExpiresAt, null, null);
    newSession.registerEvent(new SessionCreatedEvent(newSessionId, userId));
    return newSession;
  }

  private void validateRevocationConsistency() {
    if ((revokedAt == null) != (revokeReason == null)) {
      throw new IllegalStateException("revokedAt and revokeReason are inconsistent");
    }
  }

  public boolean isExpired(Instant now) {
    Objects.requireNonNull(now, "now");
    return !now.isBefore(absoluteExpiresAt);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public void revoke(Instant now, RevokeReason reason) {
    Objects.requireNonNull(now, "now");
    Objects.requireNonNull(reason, "reason");

    if (revokedAt != null && revokeReason != reason) {
      throw new SessionRevokedException();
    }
    this.revokedAt = now;
    this.revokeReason = reason;
    registerEvent(new SessionRevokedEvent(id, reason));
  }

  public SessionId id() {
    return id;
  }

  public RefreshTokenHash refreshTokenHash() {
    return refreshTokenHash;
  }

  public UUID userId() {
    return userId;
  }

  public String deviceId() {
    return deviceId;
  }

  public FamilyId familyId() {
    return familyId;
  }

  public SessionId rotatedFromSessionId() {
    return rotatedFromSessionId;
  }

  public Instant absoluteExpiresAt() {
    return absoluteExpiresAt;
  }

  public Instant revokedAt() {
    return revokedAt;
  }

  public RevokeReason revokeReason() {
    return revokeReason;
  }
}
