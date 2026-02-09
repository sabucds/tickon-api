package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.time.Instant;

public record SessionCreatedEvent(SessionId sessionId, UserId userId, Instant occurredOn) implements DomainEvent {

  public SessionCreatedEvent(SessionId sessionId, UserId userId) {
    this(sessionId, userId, Instant.now());
  }
}
