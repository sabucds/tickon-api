package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

  @Override
  public LoginResult login(LoginCommand command) {
    return null;
  }

}
