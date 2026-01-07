package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class InvalidCredentialsException extends DomainException {
  public InvalidCredentialsException() {
    super(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
  }
}
