package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class SessionRevokedException extends DomainException {
  public SessionRevokedException() {
    super(ErrorCode.SESSION_REVOKED, "Session is already revoked for a different reason");
  }
}
