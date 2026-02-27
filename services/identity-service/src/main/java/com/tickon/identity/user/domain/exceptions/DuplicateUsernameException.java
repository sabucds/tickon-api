package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class DuplicateUsernameException extends IdentityDomainException {
  private final String username;

  public DuplicateUsernameException(String username) {
    super(IdentityExceptionCodes.DUPLICATE_USERNAME);
    this.username = username;
  }

  public String username() {
    return username;
  }
}
