package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PasswordResetRequestedEvent(UUID userId, String email, String plainToken, Instant occurredOn)
    implements DomainEvent {

  public PasswordResetRequestedEvent(UUID userId, String email, String plainToken) {
    this(userId, email, plainToken, Instant.now());
  }
}
