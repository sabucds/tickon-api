package com.tickon.identityservice.user.domain;

public record Username(String value) {
  public Username {
    if (value == null
        || !value.matches("^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$")) {
      throw new IllegalArgumentException("Invalid username");
    }
  }
}
