package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidCredentialsException extends IdentityDomainException {
  public InvalidCredentialsException() {
    super(IdentityExceptionCodes.INVALID_CREDENTIALS);
  }
}
