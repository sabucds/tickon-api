package com.tickon.identity.auth.domain.exceptions;

import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;

public class SessionRevokedException extends IdentityDomainException {
  public SessionRevokedException() {
    super(IdentityExceptionCodes.SESSION_REVOKED, "Session is already revoked for a different reason");
  }
}
