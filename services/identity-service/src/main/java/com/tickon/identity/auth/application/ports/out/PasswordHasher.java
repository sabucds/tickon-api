package com.tickon.identity.auth.application.ports.out;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;

public interface PasswordHasher {

  boolean verify(String rawPassword, PasswordHash hash);
}
