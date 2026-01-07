package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class DuplicateEmailException extends DomainException {
  public DuplicateEmailException(String email) {
    super(ErrorCode.DUPLICATE_EMAIL, "Email already in use: " + email);
  }
}
