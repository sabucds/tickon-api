package com.tickon.identity.auth.domain;

import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.time.Instant;

public class Session {
  private final SessionId id;
  private final String refreshToken;
  private final UserId userId;
  private final Instant expiresAt;
  private boolean isValid;
  private final Instant createdAt;
  private Instant updatedAt;
  private Instant invalidatedAt;

  private Session(SessionId id, String refreshToken, UserId userId, Instant expiresAt, boolean isValid,
      Instant createdAt, Instant updatedAt, Instant invalidatedAt) {
    this.id = id;
    this.refreshToken = refreshToken;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.isValid = isValid;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.invalidatedAt = invalidatedAt;
  }

  public static Session create(SessionId id, String refreshToken, UserId userId, Instant now, java.time.Duration ttl) {
    Instant expiresAt = now.plus(ttl);
    return new Session(id, refreshToken, userId, expiresAt, true, now, now, null);
  }

  public static Session fromPersistence(SessionId id, String refreshToken, UserId userId, Instant expiresAt,
      boolean isValid, Instant createdAt, Instant updatedAt, Instant invalidatedAt) {
    return new Session(id, refreshToken, userId, expiresAt, isValid, createdAt, updatedAt, invalidatedAt);
  }

  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }

  public boolean isActive(Instant now) {
    return isValid && !isExpired(now);
  }

  public void invalidate(Instant now) {
    this.isValid = false;
    this.invalidatedAt = now;
    this.updatedAt = now;
  }

  public SessionId id() {
    return id;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public UserId userId() {
    return userId;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public boolean isValid() {
    return isValid;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public Instant invalidatedAt() {
    return invalidatedAt;
  }

  public void invalidate() {
    this.isValid = false;
    this.invalidatedAt = Instant.now();
  }
}
