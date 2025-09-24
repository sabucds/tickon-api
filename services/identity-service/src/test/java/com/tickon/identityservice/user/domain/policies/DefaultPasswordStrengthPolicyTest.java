package com.tickon.identityservice.user.domain.policies;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultPasswordStrengthPolicyTest {

  private DefaultPasswordStrengthPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new DefaultPasswordStrengthPolicy();
  }

  @Test
  void shouldAcceptValidPassword() {
    String validPassword = "StrongP@ss1";

    assertThatCode(() -> policy.validate(validPassword)).doesNotThrowAnyException();
  }

  @Test
  void shouldRejectNullPassword() {
    assertThatThrownBy(() -> policy.validate(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must be at least 8 characters long");
  }

  @Test
  void shouldRejectShortPassword() {
    String shortPassword = "Short1!";

    assertThatThrownBy(() -> policy.validate(shortPassword)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must be at least 8 characters long");
  }

  @Test
  void shouldRejectPasswordWithoutUppercase() {
    String passwordWithoutUppercase = "strongp@ss1";

    assertThatThrownBy(() -> policy.validate(passwordWithoutUppercase)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must contain at least one uppercase letter");
  }

  @Test
  void shouldRejectPasswordWithoutLowercase() {
    String passwordWithoutLowercase = "STRONGP@SS1";

    assertThatThrownBy(() -> policy.validate(passwordWithoutLowercase)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must contain at least one lowercase letter");
  }

  @Test
  void shouldRejectPasswordWithoutDigit() {
    String passwordWithoutDigit = "StrongP@ss";

    assertThatThrownBy(() -> policy.validate(passwordWithoutDigit)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must contain at least one digit");
  }

  @Test
  void shouldRejectPasswordWithoutSpecialCharacter() {
    String passwordWithoutSpecialChar = "StrongPass1";

    assertThatThrownBy(() -> policy.validate(passwordWithoutSpecialChar)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must contain at least one special character");
  }

  @Test
  void shouldAcceptPasswordWithMinimumLength() {
    String minimumLengthPassword = "Strong1!";

    assertThatCode(() -> policy.validate(minimumLengthPassword)).doesNotThrowAnyException();
  }

  @Test
  void shouldAcceptPasswordWithAllSpecialCharacters() {
    String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()_+-=[]{}|;:,.<>?";

    assertThatCode(() -> policy.validate(passwordWithSpecialChars)).doesNotThrowAnyException();
  }

  @Test
  void shouldRejectEmptyPassword() {
    assertThatThrownBy(() -> policy.validate("")).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Password must be at least 8 characters long");
  }
}
