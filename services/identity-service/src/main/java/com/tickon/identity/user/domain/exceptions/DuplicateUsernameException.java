package com.tickon.identity.user.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class DuplicateUsernameException extends DomainException {
  public DuplicateUsernameException(String username) {
    super(ErrorCode.DUPLICATE_USERNAME, "Username already in use: " + username);
  }
}
