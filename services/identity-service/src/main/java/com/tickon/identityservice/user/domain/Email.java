package com.tickon.identityservice.user.domain;

public record Email(String value) {
  public Email {
    if (value == null || !value.matches("^[^@]+@[^@]+\\.[^@]+$")) {
      throw new IllegalArgumentException("Invalid email");
    }
  }
}
