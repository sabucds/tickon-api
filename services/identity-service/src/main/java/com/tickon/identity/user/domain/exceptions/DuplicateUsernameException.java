package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class DuplicateUsernameException extends IdentityDomainException {
  public DuplicateUsernameException(String username) {
    super(IdentityExceptionCodes.DUPLICATE_USERNAME, "Username already in use: " + username);
  }
}
