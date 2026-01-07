package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class InvalidEmailException extends DomainException {
  public InvalidEmailException(String email) {
    super(ErrorCode.INVALID_EMAIL, "Invalid email: " + email);
  }
}
