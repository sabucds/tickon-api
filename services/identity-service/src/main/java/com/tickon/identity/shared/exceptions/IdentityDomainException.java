package com.tickon.identity.shared.exceptions;

public abstract class IdentityDomainException extends RuntimeException {
  private final IdentityExceptionCodes code;

  protected IdentityDomainException(IdentityExceptionCodes code, String message) {
    super(message);
    this.code = code;
  }

  public IdentityExceptionCodes code() {
    return code;
  }
}
