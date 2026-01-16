package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.errors.DomainException;
import com.tickon.identity.shared.errors.ErrorCode;

public class SessionExpiredException extends DomainException {
  public SessionExpiredException() {
    super(ErrorCode.SESSION_EXPIRED, "Session is already expired");
  }
}
