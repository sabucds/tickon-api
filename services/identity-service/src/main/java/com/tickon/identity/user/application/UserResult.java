package com.tickon.identity.user.application;

import com.tickon.identity.user.domain.User;

public record UserResult(String id, String username, String email, String firstName, String lastName) {
  public static UserResult from(User user) {
    return new UserResult(user.id().value().toString(), user.username().value(), user.email().value(), user.firstName(),
        user.lastName());
  }
}
