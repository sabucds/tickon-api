package com.tickon.identity.auth.application.ports;

import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;

public interface ResetTokenHasher {
  ResetTokenHash hash(String plainToken);
}
