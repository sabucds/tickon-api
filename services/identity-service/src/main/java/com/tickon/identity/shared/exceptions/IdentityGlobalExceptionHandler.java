package com.tickon.identity.shared.exceptions;

import com.tickon.common.exceptions.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class IdentityGlobalExceptionHandler extends GlobalExceptionHandler {

  @ExceptionHandler(IdentityDomainException.class)
  public ResponseEntity<ApiError> handleDomain(IdentityDomainException ex) {
    IdentityExceptionCodes code = ex.code();
    return ResponseEntity.status(code.status()).body(new ApiError(code.name(), ex.getMessage(), null));
  }

}
