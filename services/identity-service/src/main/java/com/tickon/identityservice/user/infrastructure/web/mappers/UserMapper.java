package com.tickon.identityservice.user.infrastructure.web.mappers;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService.RegisterUserCommand;
import com.tickon.identityservice.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identityservice.user.infrastructure.web.dto.UserResponse;

public final class UserMapper {
  private UserMapper() {}

  public static RegisterUserCommand toCommand(RegisterUserRequest request) {
    return new RegisterUserCommand(
        request.firstName(),
        request.lastName(),
        request.username(),
        request.email(),
        request.password()); 
  }

  public static UserResponse toDto(UserResponseModel model) {
    return new UserResponse(
        model.id(), model.firstName(), model.lastName(), model.username(), model.email());
  }
}
