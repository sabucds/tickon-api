package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidPasswordException extends IdentityDomainException {
  private final PasswordViolation violation;

  public InvalidPasswordException(PasswordViolation violation) {
    super(IdentityExceptionCodes.INVALID_PASSWORD);
    this.violation = violation;
  }

  public PasswordViolation violation() {
    return violation;
  }

  @Override
  public String messageKey() {
    return "identity.error.INVALID_PASSWORD." + violation.name();
  }
}
