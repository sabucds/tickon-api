package com.tickon.identity.auth.domain.valueobjects;

public record RefreshTokenHash(String value) {
  public RefreshTokenHash {
    if (value == null || value.isEmpty() || value.isBlank()) {
      throw new IllegalArgumentException("Refresh token hash cannot be null or empty");
    }
  }

  public static RefreshTokenHash from(String value) {
    return new RefreshTokenHash(value);
  }

}
