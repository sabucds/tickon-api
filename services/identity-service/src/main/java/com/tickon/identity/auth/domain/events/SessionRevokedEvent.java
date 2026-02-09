package com.tickon.identity.auth.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.time.Instant;

public record SessionRevokedEvent(SessionId sessionId, RevokeReason reason, Instant occurredOn) implements DomainEvent {

  public SessionRevokedEvent(SessionId sessionId, RevokeReason reason) {
    this(sessionId, reason, Instant.now());
  }
}
