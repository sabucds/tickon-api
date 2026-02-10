package com.tickon.identity.auth.domain.valueobjects;

import java.util.UUID;

public record ResetTokenId(UUID value) {
  public ResetTokenId {
    if (value == null) {
      throw new IllegalArgumentException("Invalid ResetTokenId: null value");
    }
  }

  public static ResetTokenId from(UUID uuid) {
    return new ResetTokenId(uuid);
  }

  public static ResetTokenId generate() {
    return new ResetTokenId(UUID.randomUUID());
  }
}
