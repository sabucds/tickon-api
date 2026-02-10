package com.tickon.identity.auth.domain.valueobjects;

public record ResetTokenHash(String value) {
  public ResetTokenHash {
    if (value == null || value.isEmpty() || value.isBlank()) {
      throw new IllegalArgumentException("Reset token hash cannot be null or empty");
    }
  }

  public static ResetTokenHash from(String value) {
    return new ResetTokenHash(value);
  }
}
