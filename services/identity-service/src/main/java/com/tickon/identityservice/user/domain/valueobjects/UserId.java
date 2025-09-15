package com.tickon.identityservice.user.domain.valueobjects;

import java.util.UUID;

public class UserId {
  private final UUID value;

  private UserId(UUID value) {
    this.value = value;
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }

  public static UserId from(String value) {
    return new UserId(UUID.fromString(value));
  }

  public UUID value() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserId userId = (UserId) o;
    return value.equals(userId.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
