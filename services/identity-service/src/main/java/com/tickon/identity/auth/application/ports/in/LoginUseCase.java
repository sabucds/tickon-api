package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;

public interface LoginUseCase {
  LoginResult login(LoginCommand command);
}