package com.tickon.identity.user.domain.valueobjects;

public record Email(String value) {
  public Email {
    if (value == null || !value.matches("^[^@]+@[^@]+\\.[^@]+$")) {
      throw new IllegalArgumentException("Invalid email");
    }
  }

  public static Email from(String value) {
    return new Email(value);
  }
}
