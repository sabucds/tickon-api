package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import java.time.Instant;
import java.util.UUID;

public record PasswordResetCompletedEvent(UUID userId, ResetTokenId resetTokenId, Instant occurredOn)
    implements DomainEvent {

  public PasswordResetCompletedEvent(UUID userId, ResetTokenId resetTokenId) {
    this(userId, resetTokenId, Instant.now());
  }
}
