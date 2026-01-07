package com.tickon.identity.auth.domain.valueobjects;

import com.tickon.identity.shared.errors.InvalidIdException;
import java.util.UUID;

public record FamilyId(UUID value) {
  public FamilyId {
    if (value == null)
      throw new InvalidIdException("Invalid FamilyId: null value");
  }

  public static FamilyId from(String s) {
    if (s == null || s.isEmpty())
      throw new InvalidIdException("Invalid UserId: null or empty string");
    if (!s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
      throw new InvalidIdException("Invalid UserId: not a valid UUID format");
    return new FamilyId(UUID.fromString(s));
  }

  public static FamilyId generate() {
    return new FamilyId(UUID.randomUUID());
  }
}
