package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class InvalidUsernameException extends IdentityDomainException {
  private final String username;

  public InvalidUsernameException(String username) {
    super(IdentityExceptionCodes.INVALID_USERNAME);
    this.username = username;
  }

  public String username() {
    return username;
  }
}
