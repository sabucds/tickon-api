package com.tickon.identity.shared.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.tickon.common.exceptions.GlobalExceptionHandler.ApiError;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.shared.kernel.exceptions.IdentityDomainException;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

class IdentityGlobalExceptionHandlerTest {

  @AfterEach
  void tearDown() {
    LocaleContextHolder.resetLocaleContext();
  }

  @Test
  void handleDomainReturnsLocalizedMessageForLocale() {
    Locale locale = Locale.forLanguageTag("es");
    LocaleContextHolder.setLocale(locale);

    IdentityDomainException ex = new InvalidCredentialsException();
    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage("identity.error.INVALID_CREDENTIALS", locale, "Credenciales inválidas");
    IdentityGlobalExceptionHandler handler = new IdentityGlobalExceptionHandler(messageSource);

    ResponseEntity<ApiError> response = handler.handleDomain(ex);

    assertThat(response.getStatusCode()).isEqualTo(ex.code().status());
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo(ex.code().name());
    assertThat(response.getBody().message()).isEqualTo("Credenciales inválidas");
  }
}
