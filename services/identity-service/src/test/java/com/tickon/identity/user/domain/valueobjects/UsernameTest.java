package com.tickon.identity.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.identity.user.domain.exceptions.InvalidUsernameException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UsernameTest {

  @ParameterizedTest
  @ValueSource(strings = { "valid_username", "user_name99" })
  void shouldCreateUsername_WhenValid(String validUsername) {
    Username username = Username.from(validUsername);
    assertThat(username.value()).isEqualTo(validUsername);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "@invalid-username.", "ab", "with.dot", "with space",
      "too-long-username_is_not_okay_because_length" })
  void shouldThrowException_WhenInvalidUsername(String invalidUsername) {
    assertThatThrownBy(() -> Username.from(invalidUsername)).isInstanceOf(InvalidUsernameException.class);
  }
}
