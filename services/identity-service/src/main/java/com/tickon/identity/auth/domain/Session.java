package com.tickon.identity.auth.domain;

import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.time.Duration;
import java.time.Instant;

public class Session {
  private final SessionId id;
  private final RefreshTokenHash refreshTokenHash;
  private final UserId userId;
  private final String deviceId;
  private final FamilyId familyId;
  private final SessionId rotatedFromSessionId;
  private final Instant expiresAt;
  private Instant revokedAt;
  private final RevokeReason revokeReason;
  private final Instant createdAt;
  private Instant updatedAt;

  private Session(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId, FamilyId familyId,
      SessionId rotatedFromSessionId, Instant expiresAt, Instant createdAt, Instant updatedAt, Instant revokedAt,
      RevokeReason revokeReason) {
    this.id = id;
    this.refreshTokenHash = refreshTokenHash;
    this.userId = userId;
    this.deviceId = deviceId;
    this.familyId = familyId;
    this.rotatedFromSessionId = rotatedFromSessionId;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.revokedAt = revokedAt;
    this.revokeReason = revokeReason;
  }

  public static Session create(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Instant now, Duration ttl) {
    Instant expiresAt = now.plus(ttl);
    return new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId, expiresAt, now, now,
        null, null);
  }

  public static Session fromPersistence(SessionId id, RefreshTokenHash refreshTokenHash, UserId userId, String deviceId,
      FamilyId familyId, SessionId rotatedFromSessionId, Instant expiresAt, Instant createdAt, Instant updatedAt,
      Instant revokedAt) {
    return new Session(id, refreshTokenHash, userId, deviceId, familyId, rotatedFromSessionId, expiresAt, createdAt,
        updatedAt, revokedAt, null);
  }

  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }

  public void revoke(Instant now) {
    this.revokedAt = now;
    this.updatedAt = now;
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

  public boolean isValid() {
    return !isExpired(Instant.now()) && revokedAt == null;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public Instant revokedAt() {
    return revokedAt;
  }

  public RevokeReason revokeReason() {
    return revokeReason;
  }

}
