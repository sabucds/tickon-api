package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;

public record GetUserByUsernameOrEmailQuery(String usernameOrEmail) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserByUsernameOrEmail.v1";
  }
}
