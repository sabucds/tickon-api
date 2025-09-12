package com.tickon.identityservice.user.application.models;

import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.UserId;

public record UserResponse(
    UserId id, String firstName, String lastName, String username, String email) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.id(),
        user.firstName(),
        user.lastName(),
        user.username().value(),
        user.email().value());
  }
}
