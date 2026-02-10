package com.tickon.identity.auth.domain;

import com.tickon.common.domain.AggregateRoot;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.events.PasswordResetCompletedEvent;
import com.tickon.identity.auth.domain.events.PasswordResetRequestedEvent;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.user.domain.valueobjects.Email;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class PasswordResetToken extends AggregateRoot {

  private final ResetTokenId id;
  private final ResetTokenHash tokenHash;
  private final UserId userId;
  private final Email email;
  private final Instant absoluteExpiresAt;

  private Instant usedAt;

  private PasswordResetToken(ResetTokenId id, ResetTokenHash tokenHash, UserId userId, Email email,
      Instant absoluteExpiresAt, Instant usedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.email = Objects.requireNonNull(email, "email");
    this.absoluteExpiresAt = Objects.requireNonNull(absoluteExpiresAt, "absoluteExpiresAt");
    this.usedAt = usedAt;
  }

  public static PasswordResetToken create(ResetTokenId id, ResetTokenHash tokenHash, UserId userId, Email email,
      Duration duration, Instant now) {
    Objects.requireNonNull(duration, "duration");
    Objects.requireNonNull(now, "now");
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException("Token duration must be positive");
    }

    Instant absoluteExpiresAt = now.plus(duration);
    PasswordResetToken token = new PasswordResetToken(id, tokenHash, userId, email, absoluteExpiresAt, null);
    token.registerEvent(new PasswordResetRequestedEvent(userId, email));
    return token;
  }

  public static PasswordResetToken restore(ResetTokenId id, ResetTokenHash tokenHash, UserId userId, Email email,
      Instant absoluteExpiresAt, Instant usedAt) {
    return new PasswordResetToken(id, tokenHash, userId, email, absoluteExpiresAt, usedAt);
  }

  public void markAsUsed(Instant now) {
    Objects.requireNonNull(now, "now");

    if (isUsed()) {
      throw new IllegalStateException("Password reset token has already been used");
    }

    if (isExpired(now)) {
      throw new IllegalStateException("Cannot use expired password reset token");
    }

    this.usedAt = now;
    registerEvent(new PasswordResetCompletedEvent(userId, id));
  }

  public boolean isExpired(Instant now) {
    Objects.requireNonNull(now, "now");
    return !now.isBefore(absoluteExpiresAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public ResetTokenId id() {
    return id;
  }

  public ResetTokenHash tokenHash() {
    return tokenHash;
  }

  public UserId userId() {
    return userId;
  }

  public Email email() {
    return email;
  }

  public Instant absoluteExpiresAt() {
    return absoluteExpiresAt;
  }

  public Instant usedAt() {
    return usedAt;
  }
}
