// user/domain/policy/PasswordStrengthPolicy.java
package com.tickon.identityservice.user.domain.policies;

public interface PasswordStrengthPolicy {
  void validate(String rawPassword);
}
