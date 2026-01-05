package com.tickon.identity.user.application.ports.in;

import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;

public interface RegisterUserUseCase {
  UserResult register(RegisterUserCommand command);
}
