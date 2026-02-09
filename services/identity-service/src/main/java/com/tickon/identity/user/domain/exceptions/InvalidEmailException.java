package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class InvalidEmailException extends IdentityDomainException {
  public InvalidEmailException(String email) {
    super(IdentityExceptionCodes.INVALID_EMAIL, "Invalid email: " + email);
  }
}
