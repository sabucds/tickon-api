package com.tickon.identityservice.user.domain.policies;

public interface PasswordStrengthPolicy {
  void validate(String rawPassword);
}
