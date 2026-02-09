package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.dto.RefreshTokenCommand;

public interface RefreshTokenUseCase {
  LoginResult refresh(RefreshTokenCommand command);
}
