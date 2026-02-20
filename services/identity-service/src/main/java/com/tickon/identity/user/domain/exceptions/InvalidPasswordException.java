package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidPasswordException extends IdentityDomainException {
  public InvalidPasswordException(String message) {
    super(IdentityExceptionCodes.INVALID_PASSWORD, message);
  }
}
