package com.tickon.identity.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.UserStatus;
import com.tickon.identity.user.domain.valueobjects.Username;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void shouldCreateUserWithTimestampsAndDefaults() {
    UserId userId = UserId.generate();
    User registeredUser = User.create(userId, Email.from("john@example.com"), Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"));
    assertThat(registeredUser.id()).isEqualTo(userId);
    assertThat(registeredUser.email().value()).isEqualTo("john@example.com");
    assertThat(registeredUser.username().value()).isEqualTo("john_doe");
    assertThat(registeredUser.firstName()).isEqualTo("John");
    assertThat(registeredUser.lastName()).isEqualTo("Doe");
    assertThat(registeredUser.passwordHash().value()).isEqualTo("hashed-password");
  }

  @Test
  void shouldRestore() {
    UserId userId = UserId.generate();
    User registeredUser = User.restore(userId, Email.from("john@example.com"), Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"), UserStatus.ACTIVE);

    assertThat(registeredUser.id()).isEqualTo(userId);
    assertThat(registeredUser.email().value()).isEqualTo("john@example.com");
    assertThat(registeredUser.username().value()).isEqualTo("john_doe");
    assertThat(registeredUser.firstName()).isEqualTo("John");
    assertThat(registeredUser.lastName()).isEqualTo("Doe");
    assertThat(registeredUser.passwordHash().value()).isEqualTo("hashed-password");

  }
}
