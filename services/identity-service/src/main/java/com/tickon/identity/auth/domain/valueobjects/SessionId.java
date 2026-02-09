package com.tickon.identity.auth.domain.valueobjects;

import java.util.UUID;

public record SessionId(UUID value) {
  public SessionId {
    if (value == null)
      throw new IllegalArgumentException("Invalid SessionId: null value");
  }

  public static SessionId from(String s) {
    if (s == null || s.isEmpty())
      throw new IllegalArgumentException("Invalid SessionId: null or empty string");
    if (!s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
      throw new IllegalArgumentException("Invalid SessionId: not a valid UUID format");
    return new SessionId(UUID.fromString(s));
  }

  public static SessionId generate() {
    return new SessionId(UUID.randomUUID());
  }
}
