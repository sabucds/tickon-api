package com.tickon.identity.auth.domain;

import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class Session {

  private final SessionId id;
  private final RefreshTokenHash refreshTokenHash;
  private final UserId userId;
  private final String deviceId;
  private final FamilyId familyId;
  private final SessionId rotatedFromSessionId;

  private final Instant expiresAt;

  private Instant revokedAt;
  private RevokeReason revokeReason;

  private Session(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId, FamilyId familyId,
      SessionId rotatedFromSessionId, Instant expiresAt, Instant revokedAt, RevokeReason revokeReason) {
    this.id = Objects.requireNonNull(id, "id");
    this.refreshTokenHash = Objects.requireNonNull(refreshTokenHash, "refreshTokenHash");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.deviceId = Objects.requireNonNull(deviceId, "deviceId");
    this.familyId = Objects.requireNonNull(familyId, "familyId");
    this.rotatedFromSessionId = rotatedFromSessionId;

    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");

    if (deviceId.isBlank()) {
      throw new IllegalArgumentException("deviceId cannot be blank");
    }

    if ((revokedAt == null) != (revokeReason == null)) {
      throw new IllegalArgumentException("revokedAt and revokeReason are inconsistent");
    }

    this.revokedAt = revokedAt;
    this.revokeReason = revokeReason;
  }

  public static Session create(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Instant now, Duration ttl) {
    Objects.requireNonNull(now, "now");
    Objects.requireNonNull(ttl, "ttl");
    if (ttl.isZero() || ttl.isNegative()) {
      throw new IllegalArgumentException("ttl must be positive");
    }

    return new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId, now.plus(ttl), null,
        null);
  }

  public static Session fromPersistence(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Instant expiresAt, Instant revokedAt,
      RevokeReason revokeReason) {
    return new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId, expiresAt, revokedAt,
        revokeReason);
  }

  public boolean isExpired(Instant now) {
    Objects.requireNonNull(now, "now");
    return !now.isBefore(expiresAt);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public void revoke(Instant now, RevokeReason reason) {
    Objects.requireNonNull(now, "now");
    Objects.requireNonNull(reason, "reason");

    if (revokedAt != null) {
      return;
    }
    this.revokedAt = now;
    this.revokeReason = reason;

  }

  public SessionId id() {
    return id;
  }

  public RefreshTokenHash refreshTokenHash() {
    return refreshTokenHash;
  }

  public UserId userId() {
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

  public Instant expiresAt() {
    return expiresAt;
  }

  public Instant revokedAt() {
    return revokedAt;
  }

  public RevokeReason revokeReason() {
    return revokeReason;
  }
}
