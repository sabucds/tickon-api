package com.tickon.identityservice.user.application.models;

import com.tickon.identityservice.user.domain.User;

public record UserResponseModel(
    String id, String username, String email, String firstName, String lastName) {
  public static UserResponseModel from(User user) {
    return new UserResponseModel(
        user.id().value().toString(),
        user.username().value(),
        user.email().value(),
        user.firstName(),
        user.lastName());
  }
}
