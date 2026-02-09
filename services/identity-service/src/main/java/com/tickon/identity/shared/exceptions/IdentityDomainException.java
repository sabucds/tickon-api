package com.tickon.identity.shared.exceptions;

import com.tickon.common.exceptions.DomainException;

public abstract class IdentityDomainException extends DomainException {
  private final IdentityExceptionCodes code;

  protected IdentityDomainException(IdentityExceptionCodes code, String message) {
    super(message);
    this.code = code;
  }

  public IdentityExceptionCodes code() {
    return code;
  }
}
