package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;
import java.util.Optional;

public record GetUserByEmailQuery(String email) implements Query<Optional<UserAuthDataDTO>> {
  @Override
  public String getQueryName() {
    return "GetUserByEmail.v1";
  }
}
