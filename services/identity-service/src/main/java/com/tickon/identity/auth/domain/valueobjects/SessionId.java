package com.tickon.identity.auth.domain.valueobjects;

import java.util.UUID;

public class SessionId {
  private final UUID value;

  private SessionId(UUID value) {
    this.value = value;
  }

  public static SessionId generate() {
    return new SessionId(UUID.randomUUID());
  }

  public static SessionId from(String value) {
    return new SessionId(UUID.fromString(value));
  }

  public UUID value() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SessionId sessionId = (SessionId) o;
    return value.equals(sessionId.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
