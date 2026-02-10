package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;

public record GetUserByEmailQuery(String email) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserByEmail.v1";
  }
}
