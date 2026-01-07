package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class InvalidPasswordException extends DomainException {
  public InvalidPasswordException(String message) {
    super(ErrorCode.INVALID_PASSWORD, message);
  }
}
