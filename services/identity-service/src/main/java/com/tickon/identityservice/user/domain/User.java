// user/domain/User.java
package com.tickon.identityservice.user.domain;

import java.time.Instant;

public class User {
  private final UserId id;
  private Email email;
  private Username username;
  private String firstName;
  private String lastName;
  private PasswordHash passwordHash;
  private final Instant createdAt;
  private Instant updatedAt;
  private boolean isDeleted;
  private Instant deletedAt;

  private User(UserId id, Email email, Username username, String firstName, String lastName,
      PasswordHash passwordHash, Instant createdAt, Instant updatedAt, boolean isDeleted,
      Instant deletedAt) {
    this.id = id;
    this.email = email;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }

  public static User fromPersistence(UserId id, Email email, Username username, String firstName,
      String lastName, PasswordHash passwordHash, Instant createdAt, Instant updatedAt,
      boolean isDeleted, Instant deletedAt) {
    return new User(id, email, username, firstName, lastName, passwordHash, createdAt, updatedAt,
        isDeleted, deletedAt);
  }

  public static User forRegistration(UserId id, Email email, Username username, String firstName,
      String lastName, PasswordHash passwordHash) {
    return new User(id, email, username, firstName, lastName, passwordHash, Instant.now(),
        Instant.now(), false, null);
  }

  public UserId id() {
    return id;
  }

  public Username username() {
    return username;
  }

  public String firstName() {
    return firstName;
  }

  public String lastName() {
    return lastName;
  }

  public Email email() {
    return email;
  }

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public Instant deletedAt() {
    return deletedAt;
  }

}
