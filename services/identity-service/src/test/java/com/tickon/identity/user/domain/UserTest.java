package com.tickon.identity.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.time.Instant;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTest {

  private Instant fixedInstant;

  @BeforeEach
  void setUp() {
    fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  }

  @Test
  public void shouldRegisterUser() {
    UserId userId = UserId.generate();
    User registeredUser = User.create(userId, Email.from("john@example.com"), Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"), fixedInstant);

    assertThat(registeredUser.email().value()).isEqualTo("john@example.com");
    assertThat(registeredUser.username().value()).isEqualTo("john_doe");
    assertThat(registeredUser.firstName()).isEqualTo("John");
    assertThat(registeredUser.lastName()).isEqualTo("Doe");
    assertThat(registeredUser.passwordHash().value()).isEqualTo("hashed-password");
    assertThat(registeredUser.createdAt()).isNotNull();
    assertThat(registeredUser.updatedAt()).isNotNull();
    assertThat(registeredUser.isDeleted()).isFalse();
    assertThat(registeredUser.deletedAt()).isNull();
    assertThat(registeredUser.id()).isEqualTo(userId);
    assertThat(registeredUser.createdAt()).isEqualTo(fixedInstant);
    assertThat(registeredUser.updatedAt()).isEqualTo(fixedInstant);
  }

  @Test
  public void shouldBeInstantiatedWithPersistenceData() {
    UserId userId = UserId.generate();
    User registeredUser = User.fromPersistence(userId, Email.from("john@example.com"), Username.from("john_doe"),
        "John", "Doe", new PasswordHash("hashed-password"), fixedInstant, fixedInstant, false, null);

    assertThat(registeredUser.email().value()).isEqualTo("john@example.com");
    assertThat(registeredUser.username().value()).isEqualTo("john_doe");
    assertThat(registeredUser.firstName()).isEqualTo("John");
    assertThat(registeredUser.lastName()).isEqualTo("Doe");
    assertThat(registeredUser.passwordHash().value()).isEqualTo("hashed-password");
    assertThat(registeredUser.createdAt()).isEqualTo(fixedInstant);
    assertThat(registeredUser.updatedAt()).isEqualTo(fixedInstant);
    assertThat(registeredUser.isDeleted()).isFalse();
    assertThat(registeredUser.deletedAt()).isNull();
  }
}
