package com.tickon.identity.shared.kernel.ports.out;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;

public interface PasswordHasher {

  PasswordHash hash(String rawPassword);

  boolean verify(String rawPassword, PasswordHash hash);
}
