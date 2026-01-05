package com.tickon.identity.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UsernameTest {
  @Test
  void shouldCreateUsername_WhenValid() {
    String validUsername = "valid_username";
    Username username = Username.from(validUsername);
    assertThat(username.value()).isEqualTo(validUsername);
  }

  @Test
  void shouldThrowException_WhenInvalidUsername() {
    String invalidUsername = "@invalid-username.";
    assertThatThrownBy(() -> Username.from(invalidUsername)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_WhenNullUsername() {
    assertThatThrownBy(() -> Username.from(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_WhenEmptyUsername() {
    assertThatThrownBy(() -> Username.from("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_WhenUsernameTooShort() {
    assertThatThrownBy(() -> Username.from("ab")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldCreateUsername_WhenMinimumLength() {
    Username username = Username.from("min_user");
    assertThat(username.value()).isEqualTo("min_user");
  }
}
