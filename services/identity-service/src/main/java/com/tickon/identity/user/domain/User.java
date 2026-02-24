package com.tickon.identity.user.domain;

import com.tickon.common.domain.AggregateRoot;
import com.tickon.identity.user.domain.events.UserCreatedEvent;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.UserStatus;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.util.Objects;

public class User extends AggregateRoot {

  private final UserId id;
  private Email email;
  private Username username;
  private String firstName;
  private String lastName;
  private PasswordHash passwordHash;
  private UserStatus status;

  private User(UserId id, Email email, Username username, String firstName, String lastName, PasswordHash passwordHash,
      UserStatus status) {
    this.id = Objects.requireNonNull(id, "id");
    this.email = Objects.requireNonNull(email, "email");
    this.username = Objects.requireNonNull(username, "username");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.firstName = Objects.requireNonNull(firstName, "firstName");
    this.status = Objects.requireNonNull(status, "status");
    this.lastName = Objects.requireNonNull(lastName, "lastName");
  }

  public static User create(UserId id, Email email, Username username, String firstName, String lastName,
      PasswordHash passwordHash) {
    UserStatus status = UserStatus.ACTIVE;
    User user = new User(id, email, username, firstName, lastName, passwordHash, status);
    user.registerEvent(new UserCreatedEvent(id, email));
    return user;
  }

  public static User restore(UserId id, Email email, Username username, String firstName, String lastName,
      PasswordHash passwordHash, UserStatus status) {
    return new User(id, email, username, firstName, lastName, passwordHash, status);
  }

  public void changeEmail(Email newEmail) {
    this.email = Objects.requireNonNull(newEmail, "newEmail");
  }

  public void changeUsername(Username newUsername) {
    this.username = Objects.requireNonNull(newUsername, "newUsername");
  }

  public void changePasswordHash(PasswordHash newHash) {
    this.passwordHash = Objects.requireNonNull(newHash, "newHash");
  }

  public UserId id() {
    return id;
  }

  public Email email() {
    return email;
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

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public UserStatus status() {
    return status;
  }

}
