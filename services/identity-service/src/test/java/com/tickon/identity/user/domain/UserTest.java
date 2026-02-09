package com.tickon.identity.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.user.domain.events.UserCreatedEvent;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.util.List;
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
  void shouldRegisterUserCreatedEvent_WhenCreatingUser() {
    UserId userId = UserId.generate();
    Email email = Email.from("john@example.com");
    User user = User.create(userId, email, Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"));

    List<DomainEvent> events = user.domainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(UserCreatedEvent.class);

    UserCreatedEvent event = (UserCreatedEvent) events.get(0);
    assertThat(event.userId()).isEqualTo(userId);
    assertThat(event.email()).isEqualTo(email);
    assertThat(event.occurredOn()).isNotNull();
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

  @Test
  void shouldNotRegisterEvents_WhenRestoringUser() {
    UserId userId = UserId.generate();
    User restoredUser = User.restore(userId, Email.from("john@example.com"), Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"), UserStatus.ACTIVE);

    assertThat(restoredUser.domainEvents()).isEmpty();
  }
}
