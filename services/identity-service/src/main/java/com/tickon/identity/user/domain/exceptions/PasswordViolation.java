package com.tickon.identity.user.domain.exceptions;

public enum PasswordViolation {
  TOO_SHORT, MISSING_UPPERCASE, MISSING_LOWERCASE, MISSING_DIGIT, MISSING_SPECIAL_CHAR
}
