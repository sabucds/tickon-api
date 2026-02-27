package com.tickon.identity.contracts.user.queries;

import com.tickon.common.queries.Query;
import java.util.Optional;

public record GetUserByUsernameOrEmailQuery(String usernameOrEmail) implements Query<Optional<UserAuthDataDTO>> {
  @Override
  public String getQueryName() {
    return "GetUserByUsernameOrEmail.v1";
  }
}
