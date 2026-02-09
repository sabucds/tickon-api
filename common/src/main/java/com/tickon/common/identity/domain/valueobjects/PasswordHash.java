package com.tickon.common.identity.domain.valueobjects;

public record PasswordHash(String value) {
  public PasswordHash {
    if (value == null || value.isEmpty() || value.isBlank()) {
      throw new IllegalArgumentException("Password hash cannot be null or empty");
    }
  }

  public static PasswordHash from(String value) {
    return new PasswordHash(value);
  }
}
