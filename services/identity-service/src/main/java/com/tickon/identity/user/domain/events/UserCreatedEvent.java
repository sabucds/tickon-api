package com.tickon.identity.user.domain.events;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import java.time.Instant;

public record UserCreatedEvent(UserId userId, Email email, Instant occurredOn) implements DomainEvent {

  public UserCreatedEvent(UserId userId, Email email) {
    this(userId, email, Instant.now());
  }
}
