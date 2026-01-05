package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.LoginResult;

public interface LoginUseCase {
  LoginResult login(LoginCommand command);

  public record LoginCommand(String usernameOrEmail, String password) {}
}