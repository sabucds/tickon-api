package com.tickon.identity.auth.infrastructure.web.mappers;

import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.in.LoginUseCase.LoginCommand;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;

public final class LoginMapper {
  private LoginMapper() {}

  public static LoginCommand toLoginCommand(LoginRequest request) {
    return new LoginCommand(request.usernameOrEmail(), request.password());
  }

  public static LoginResponse toLoginResponse(LoginResult model) {
    return new LoginResponse(model.accessToken(), model.refreshToken());
  }

}
