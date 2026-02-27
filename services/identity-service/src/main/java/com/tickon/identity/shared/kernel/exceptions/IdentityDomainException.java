package com.tickon.identity.shared.kernel.exceptions;

public abstract class IdentityDomainException extends RuntimeException {
  private final IdentityExceptionCodes code;

  protected IdentityDomainException(IdentityExceptionCodes code) {
    super(code.name());
    this.code = code;
  }

  public IdentityExceptionCodes code() {
    return code;
  }

  public String messageKey() {
    return "identity.error." + code.name();
  }
}
