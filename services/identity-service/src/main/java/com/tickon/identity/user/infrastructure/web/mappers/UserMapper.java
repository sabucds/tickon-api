package com.tickon.identity.user.infrastructure.web.mappers;

import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identity.user.infrastructure.web.dto.UserResponse;

public final class UserMapper {
  private UserMapper() {}

  public static RegisterUserCommand toRegisterCommand(RegisterUserRequest request) {
    return new RegisterUserCommand(request.firstName(), request.lastName(), request.username(), request.email(),
        request.password());
  }

  public static UserResponse toDto(UserResult model) {
    return new UserResponse(model.id(), model.firstName(), model.lastName(), model.username(), model.email());
  }
}
