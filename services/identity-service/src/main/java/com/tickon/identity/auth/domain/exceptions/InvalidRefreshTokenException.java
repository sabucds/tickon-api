package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class InvalidRefreshTokenException extends IdentityDomainException {
  public InvalidRefreshTokenException() {
    super(IdentityExceptionCodes.INVALID_REFRESH_TOKEN, "Invalid refresh token");
  }
}
