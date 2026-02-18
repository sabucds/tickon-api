package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import java.time.Instant;

public record PasswordResetCompletedEvent(UserId userId, ResetTokenId resetTokenId, Instant occurredOn)
    implements DomainEvent {

  public PasswordResetCompletedEvent(UserId userId, ResetTokenId resetTokenId) {
    this(userId, resetTokenId, Instant.now());
  }
}
