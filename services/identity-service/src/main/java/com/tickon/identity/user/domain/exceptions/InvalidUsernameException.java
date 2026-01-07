package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class InvalidUsernameException extends DomainException {
  public InvalidUsernameException(String username) {
    super(ErrorCode.INVALID_USERNAME, "Invalid username: " + username);
  }
}
