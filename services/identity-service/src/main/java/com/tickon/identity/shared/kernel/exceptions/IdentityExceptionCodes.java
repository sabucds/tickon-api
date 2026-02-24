package com.tickon.identity.shared.kernel.exceptions;

import org.springframework.http.HttpStatus;

public enum IdentityExceptionCodes {
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST), INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED(HttpStatus.FORBIDDEN), RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST), INVALID_EMAIL(HttpStatus.BAD_REQUEST),
  INVALID_USERNAME(HttpStatus.BAD_REQUEST), INVALID_ID(HttpStatus.BAD_REQUEST), SESSION_REVOKED(HttpStatus.UNAUTHORIZED),
  SESSION_EXPIRED(HttpStatus.UNAUTHORIZED), INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED),
  INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST),

  DUPLICATE_EMAIL(HttpStatus.CONFLICT), DUPLICATE_USERNAME(HttpStatus.CONFLICT),

  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus status;

  IdentityExceptionCodes(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus status() {
    return status;
  }
}
