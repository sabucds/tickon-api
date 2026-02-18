package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import java.time.Instant;

public record PasswordResetRequestedEvent(UserId userId, Email email, String plainToken, Instant occurredOn)
    implements DomainEvent {

  public PasswordResetRequestedEvent(UserId userId, Email email, String plainToken) {
    this(userId, email, plainToken, Instant.now());
  }
}
