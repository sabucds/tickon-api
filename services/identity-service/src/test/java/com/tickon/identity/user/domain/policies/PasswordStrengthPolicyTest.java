package com.tickon.identity.user.domain.policies;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.identity.user.domain.exceptions.InvalidPasswordException;
import com.tickon.identity.user.domain.exceptions.PasswordViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PasswordStrengthPolicyTest {

  private PasswordStrengthPolicy policy;

  @BeforeEach
  void setUp() {
    policy = new PasswordStrengthPolicy();
  }

  @ParameterizedTest
  @EnumSource(PasswordViolation.class)
  void shouldThrowInvalidPasswordWithViolation(PasswordViolation violation) {
    String invalidPassword = passwordForViolation(violation);

    assertThatThrownBy(() -> policy.validate(invalidPassword)).isInstanceOf(InvalidPasswordException.class)
        .satisfies(ex -> {
          InvalidPasswordException invalid = (InvalidPasswordException) ex;
          org.assertj.core.api.Assertions.assertThat(invalid.violation()).isEqualTo(violation);
        });
  }

  @Test
  void shouldAcceptValidPasswordWhenAllRulesMet() {
    assertThatCode(() -> policy.validate("Strong1!")).doesNotThrowAnyException();
  }

  private String passwordForViolation(PasswordViolation violation) {
    return switch (violation) {
    case TOO_SHORT -> "Aa1!aaa";
    case MISSING_UPPERCASE -> "lowercase1!";
    case MISSING_LOWERCASE -> "UPPERCASE1!";
    case MISSING_DIGIT -> "NoDigit!A";
    case MISSING_SPECIAL_CHAR -> "NoSpecial1A";
    };
  }
}
