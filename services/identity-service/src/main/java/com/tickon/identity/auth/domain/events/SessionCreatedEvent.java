package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.time.Instant;
import java.util.UUID;

public record SessionCreatedEvent(SessionId sessionId, UUID userId, Instant occurredOn) implements DomainEvent {

  public SessionCreatedEvent(SessionId sessionId, UUID userId) {
    this(sessionId, userId, Instant.now());
  }
}
