package com.tickon.identityservice.user.application.ports.inbound;

import com.tickon.identityservice.user.application.models.UserResponseModel;

public interface RegisterUserService {
  UserResponseModel register(RegisterUserCommand request);

  public record RegisterUserCommand(
      String firstName, String lastName, String username, String email, String rawPassword) {}
}
