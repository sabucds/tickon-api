package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidRefreshTokenException extends IdentityDomainException {
  public InvalidRefreshTokenException() {
    super(IdentityExceptionCodes.INVALID_REFRESH_TOKEN);
  }
}
