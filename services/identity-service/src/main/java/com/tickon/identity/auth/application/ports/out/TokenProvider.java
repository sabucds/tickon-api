package com.tickon.identity.auth.application.ports.out;

import com.tickon.identity.auth.domain.AuthUser;

public interface TokenProvider {

  String generateAccessToken(AuthUser user);

  String generateRefreshToken(AuthUser user);
}
