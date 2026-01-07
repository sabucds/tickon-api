package com.tickon.identity.shared.errors;

public class InvalidIdException extends DomainException {
  public InvalidIdException(String message) {
    super(ErrorCode.INVALID_ID, message);
  }
}
