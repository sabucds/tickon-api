package com.tickon.common.identity.domain.valueobjects;

public record Email(String value) {
  public Email {
    if (value == null || !value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
      throw new IllegalArgumentException("Invalid email: " + value);
    }
  }

  public static Email from(String value) {
    return new Email(value);
  }
}
