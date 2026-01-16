package com.tickon.identity.shared.errors;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

    return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_FAILED", "Validation failed", errors));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ApiError> handleDomain(DomainException ex) {
    ErrorCode code = ex.code();
    return ResponseEntity.status(code.status()).body(new ApiError(code.name(), ex.getMessage(), null));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of("ACCESS_DENIED", "Access denied", null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiError.of("INTERNAL_ERROR", "Something went wrong", null));
  }

  public record ApiError(String code, String message, Map<String, String> errors) {
    public static ApiError of(String code, String message, Map<String, String> errors) {
      return new ApiError(code, message, errors);
    }
  }
}
