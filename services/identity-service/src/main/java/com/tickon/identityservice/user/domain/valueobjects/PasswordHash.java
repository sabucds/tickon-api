package com.tickon.identityservice.user.domain.valueobjects;

public class PasswordHash {
  private final String value;

  public PasswordHash(String value) {
    if (value == null || value.isEmpty() || value.isBlank()) {
      throw new IllegalArgumentException("Password hash cannot be null or empty");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    PasswordHash that = (PasswordHash) o;
    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
