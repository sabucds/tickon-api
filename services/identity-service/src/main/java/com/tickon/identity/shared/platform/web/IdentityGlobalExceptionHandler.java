package com.tickon.identity.shared.platform.web;

import com.tickon.common.exceptions.GlobalExceptionHandler;
import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;
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
