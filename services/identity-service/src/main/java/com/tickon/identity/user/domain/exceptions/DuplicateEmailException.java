package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class DuplicateEmailException extends IdentityDomainException {
  public DuplicateEmailException(String email) {
    super(IdentityExceptionCodes.DUPLICATE_EMAIL, "Email already in use: " + email);
  }
}
