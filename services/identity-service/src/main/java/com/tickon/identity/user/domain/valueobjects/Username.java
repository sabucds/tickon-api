package com.tickon.identity.user.domain.valueobjects;

import com.tickon.identity.user.domain.exceptions.InvalidUsernameException;

public record Username(String value) {
  public Username {
    if (value == null || !value.matches("^[A-Za-z][A-Za-z0-9_]{5,29}$")) {
      throw new InvalidUsernameException(value);
    }
  }

  public static Username from(String value) {
    return new Username(value);
  }
}
