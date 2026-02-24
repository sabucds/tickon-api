package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidEmailException extends IdentityDomainException {
  private final String email;

  public InvalidEmailException(String email) {
    super(IdentityExceptionCodes.INVALID_EMAIL, "Invalid email");
    this.email = email;
  }

  public String email() {
    return email;
  }
}
