package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.exceptions.IdentityDomainException;
import com.tickon.identity.shared.exceptions.IdentityExceptionCodes;

public class SessionExpiredException extends IdentityDomainException {
  public SessionExpiredException() {
    super(IdentityExceptionCodes.SESSION_EXPIRED, "Session is already expired");
  }
}
