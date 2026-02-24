package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class DuplicateEmailException extends IdentityDomainException {
  private final String email;

  public DuplicateEmailException(String email) {
    super(IdentityExceptionCodes.DUPLICATE_EMAIL, "Email already in use");
    this.email = email;
  }

  public String email() {
    return email;
  }
}
