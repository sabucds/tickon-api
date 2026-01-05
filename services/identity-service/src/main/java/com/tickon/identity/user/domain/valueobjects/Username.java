package com.tickon.identity.user.domain.valueobjects;

public record Username(String value) {
  public Username {
    if (value == null || !value.matches("^[A-Za-z][A-Za-z0-9_]{5,29}$")) {
      throw new IllegalArgumentException("Invalid username");
    }
  }

  public static Username from(String value) {
    return new Username(value);
  }
}
