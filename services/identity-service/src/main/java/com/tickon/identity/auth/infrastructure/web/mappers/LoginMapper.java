package com.tickon.identity.auth.infrastructure.web.mappers;

import com.tickon.identity.auth.application.command.login.LoginCommand;
import com.tickon.identity.auth.application.command.login.LoginResult;
import com.tickon.identity.auth.application.command.logout.LogoutCommand;
import com.tickon.identity.auth.application.command.refresh.RefreshTokenCommand;
import com.tickon.identity.auth.application.command.refresh.RefreshTokenResult;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;
import com.tickon.identity.auth.infrastructure.web.dto.LogoutRequest;
import com.tickon.identity.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.tickon.identity.auth.infrastructure.web.dto.RefreshTokenResponse;

public final class LoginMapper {
  private LoginMapper() {}

  public static LoginCommand toLoginCommand(LoginRequest request) {
    return new LoginCommand(request.usernameOrEmail(), request.password(), request.deviceId());
  }

  public static LoginResponse toLoginResponse(LoginResult model) {
    return new LoginResponse(model.accessToken(), model.refreshToken());
  }

  public static RefreshTokenCommand toRefreshTokenCommand(RefreshTokenRequest request) {
    return new RefreshTokenCommand(request.refreshToken());
  }

  public static LogoutCommand toLogoutCommand(LogoutRequest request) {
    return new LogoutCommand(request.refreshToken());
  }

  public static RefreshTokenResponse toRefreshTokenResponse(RefreshTokenResult model) {
    return new RefreshTokenResponse(model.accessToken(), model.refreshToken());
  }
}
