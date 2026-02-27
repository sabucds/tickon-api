package com.tickon.identity.user.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class InvalidPasswordExceptionTest {

  @ParameterizedTest
  @EnumSource(PasswordViolation.class)
  void shouldExposeMessageKeyAndCodeForEachViolation(PasswordViolation violation) {
    InvalidPasswordException exception = new InvalidPasswordException(violation);

    assertThat(exception.code()).isEqualTo(IdentityExceptionCodes.INVALID_PASSWORD);
    assertThat(exception.violation()).isEqualTo(violation);
    assertThat(exception.messageKey()).isEqualTo("identity.error.INVALID_PASSWORD." + violation.name());
  }
}
