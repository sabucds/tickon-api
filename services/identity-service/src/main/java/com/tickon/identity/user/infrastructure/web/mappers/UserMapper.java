package com.tickon.identity.user.infrastructure.web.mappers;

import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.command.register.RegisterUserCommand;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.Username;
import com.tickon.identity.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identity.user.infrastructure.web.dto.UserResponse;

public final class UserMapper {
  private UserMapper() {}

  public static RegisterUserCommand toRegisterCommand(RegisterUserRequest req) {
    return new RegisterUserCommand(req.firstName(), req.lastName(), Username.from(req.username()),
        Email.from(req.email()), req.password());
  }

  public static UserResponse toDto(UserResult model) {
    return new UserResponse(model.id(), model.firstName(), model.lastName(), model.username(), model.email());
  }
}
