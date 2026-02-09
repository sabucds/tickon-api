package com.tickon.identity.user.domain.valueobjects;

import com.tickon.identity.user.domain.exceptions.InvalidEmailException;

public record Email(String value) {
  public Email {
    if (value == null || !value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
      throw new InvalidEmailException(value);
    }
  }

  public static Email from(String value) {
    return new Email(value);
  }
}
