package com.tickon.identity.user.application.ports.out;

import com.tickon.identity.user.domain.valueobjects.PasswordHash;

public interface PasswordHasher {
  PasswordHash hash(String rawPassword);

  boolean verify(String rawPassword, PasswordHash hash);
}
