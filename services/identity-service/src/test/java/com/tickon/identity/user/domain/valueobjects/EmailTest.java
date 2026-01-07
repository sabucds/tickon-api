package com.tickon.identity.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.identity.user.domain.exceptions.InvalidEmailException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

  @ParameterizedTest
  @ValueSource(strings = { "john_doe@gmail.com", "user+alias@domain.co", "a.b@sub.domain.com" })
  void shouldCreateEmail_WhenValid(String validEmail) {
    Email email = Email.from(validEmail);
    assertThat(email.value()).isEqualTo(validEmail);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "john_doegmail.com", "no-domain@", "no-at.com", "with spaces@example.com" })
  void shouldThrowException_WhenInvalidEmail(String invalidEmail) {
    assertThatThrownBy(() -> Email.from(invalidEmail)).isInstanceOf(InvalidEmailException.class);
  }
}
