package com.tickon.identity.auth.application.ports;

import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;

public interface RefreshTokenHasher {
  RefreshTokenHash hash(String refreshToken);
}