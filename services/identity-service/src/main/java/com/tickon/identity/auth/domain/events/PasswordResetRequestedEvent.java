package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Email;
import java.time.Instant;

public record PasswordResetRequestedEvent(UserId userId, Email email, Instant occurredOn) implements DomainEvent {

  public PasswordResetRequestedEvent(UserId userId, Email email) {
    this(userId, email, Instant.now());
  }
}
