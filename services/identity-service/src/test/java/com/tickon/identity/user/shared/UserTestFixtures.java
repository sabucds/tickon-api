package com.tickon.identity.user.shared;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.Username;

public final class UserTestFixtures {
  private UserTestFixtures() {}

  public static User aUser() {
    return User.create(UserId.generate(), Email.from("john@example.com"), Username.from("johnny_doe"), "John", "Doe",
        new PasswordHash("hash"));
  }

  public static User aUserWithId(String id) {
    return User.create(UserId.from(id), Email.from("john@example.com"), Username.from("johnny_doe"), "John", "Doe",
        new PasswordHash("hash"));
  }
}