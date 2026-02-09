package com.tickon.identity.user.domain.policies;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.identity.user.domain.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DefaultPasswordStrengthPolicyTest {

  private PasswordStrengthPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new PasswordStrengthPolicy();
  }

  @ParameterizedTest
  @ValueSource(strings = { "StrongP@ss1", "P@ssw0rd!#$%^", "Aa1!aaaa" })
  void shouldAcceptValidPassword(String validPassword) {
    assertThatCode(() -> policy.validate(validPassword)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "Short1!", "strongp@ss1", "STRONGP@SS1", "StrongP@ss", "StrongPass1" })
  void shouldRejectInvalidPassword(String invalidPassword) {
    assertThatThrownBy(() -> policy.validate(invalidPassword)).isInstanceOf(InvalidPasswordException.class);
  }
}
