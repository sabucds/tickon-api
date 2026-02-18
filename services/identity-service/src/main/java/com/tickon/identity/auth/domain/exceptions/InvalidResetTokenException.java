package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class InvalidResetTokenException extends IdentityDomainException {
  public InvalidResetTokenException() {
    super(IdentityExceptionCodes.INVALID_RESET_TOKEN, "Invalid or expired reset token");
  }
}
