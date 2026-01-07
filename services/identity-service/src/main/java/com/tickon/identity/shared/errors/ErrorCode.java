package com.tickon.identity.shared.errors;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST), INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED(HttpStatus.FORBIDDEN), RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST), INVALID_EMAIL(HttpStatus.BAD_REQUEST),
  INVALID_USERNAME(HttpStatus.BAD_REQUEST), INVALID_ID(HttpStatus.BAD_REQUEST),

  DUPLICATE_EMAIL(HttpStatus.CONFLICT), DUPLICATE_USERNAME(HttpStatus.CONFLICT),

  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus status() {
    return status;
  }
}
