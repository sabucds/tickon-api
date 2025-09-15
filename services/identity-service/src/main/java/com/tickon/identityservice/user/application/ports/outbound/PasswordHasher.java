package com.tickon.identityservice.user.application.ports.outbound;

import com.tickon.identityservice.user.domain.valueobjects.PasswordHash;

public interface PasswordHasher {
  PasswordHash hash(String rawPassword);

  boolean verify(String rawPassword, PasswordHash hash);
}
