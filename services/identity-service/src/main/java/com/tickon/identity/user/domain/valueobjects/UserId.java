package com.tickon.identity.user.domain.valueobjects;

import com.tickon.identity.shared.errors.InvalidIdException;
import java.util.UUID;

public record UserId(UUID value) {
  public UserId {
    if (value == null)
      throw new InvalidIdException("Invalid UserId: null value");
  }

  public static UserId from(String s) {
    if (s == null || s.isEmpty())
      throw new InvalidIdException("Invalid UserId: null or empty string");
    if (!s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
      throw new InvalidIdException("Invalid UserId: not a valid UUID format");
    return new UserId(UUID.fromString(s));
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
