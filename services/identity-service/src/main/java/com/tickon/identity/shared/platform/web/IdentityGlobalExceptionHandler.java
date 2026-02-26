package com.tickon.identity.shared.platform.web;

import com.tickon.common.exceptions.GlobalExceptionHandler;
import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class IdentityGlobalExceptionHandler extends GlobalExceptionHandler {

  private final MessageSource messageSource;

  public IdentityGlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(IdentityDomainException.class)
  public ResponseEntity<ApiError> handleDomain(IdentityDomainException ex) {
    IdentityExceptionCodes code = ex.code();
    String messageKey = "identity.error." + code.name();
    String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    return ResponseEntity.status(code.status()).body(new ApiError(code.name(), message, null));
  }

}
