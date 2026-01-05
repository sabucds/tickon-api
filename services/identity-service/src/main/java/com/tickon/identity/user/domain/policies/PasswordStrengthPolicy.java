package com.tickon.identity.user.domain.policies;

import org.springframework.stereotype.Component;

@Component
public class PasswordStrengthPolicy {

  private static final int MIN_LENGTH = 8;
  private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
  private static final String LOWERCASE_PATTERN = ".*[a-z].*";
  private static final String DIGIT_PATTERN = ".*\\d.*";
  private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*";

  public void validate(String rawPassword) {
    if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
      throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
    }

    if (!rawPassword.matches(UPPERCASE_PATTERN)) {
      throw new IllegalArgumentException("Password must contain at least one uppercase letter");
    }

    if (!rawPassword.matches(LOWERCASE_PATTERN)) {
      throw new IllegalArgumentException("Password must contain at least one lowercase letter");
    }

    if (!rawPassword.matches(DIGIT_PATTERN)) {
      throw new IllegalArgumentException("Password must contain at least one digit");
    }

    if (!rawPassword.matches(SPECIAL_CHAR_PATTERN)) {
      throw new IllegalArgumentException("Password must contain at least one special character");
    }
  }
}
