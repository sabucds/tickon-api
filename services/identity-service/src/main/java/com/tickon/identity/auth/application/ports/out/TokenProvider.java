package com.tickon.identity.auth.application.ports.out;

import com.tickon.identity.user.domain.User;

public interface TokenProvider {

  String generateAccessToken(User user);

  String generateRefreshToken(User user);
}
