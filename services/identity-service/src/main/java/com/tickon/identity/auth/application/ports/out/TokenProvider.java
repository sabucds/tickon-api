package com.tickon.identity.auth.application.ports.out;

import java.util.UUID;

public interface TokenProvider {

  String generateAccessToken(UUID userId);

  String generateRefreshToken();
}
