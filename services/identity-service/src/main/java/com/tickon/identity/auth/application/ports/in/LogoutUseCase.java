package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.LogoutCommand;

public interface LogoutUseCase {
  void logout(LogoutCommand command);
}
