package com.tickon.identityservice.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {
  @Test
  void shouldCreateEmail_WhenValid() {
    String validEmail = "john_doe@gmail.com";
    Email email = Email.from(validEmail);
    assertThat(email.value()).isEqualTo(validEmail);
  }

  @Test
  void shouldThrowException_WhenInvalidEmail() {
    String invalidEmail = "john_doegmail.com";
    assertThatThrownBy(() -> Email.from(invalidEmail)).isInstanceOf(IllegalArgumentException.class);
  }
}
