package com.tickon.identity.contracts.user.queries;

import com.tickon.common.queries.Query;
import java.util.Optional;
import java.util.UUID;

public record GetUserAuthDataQuery(UUID userId) implements Query<Optional<UserAuthDataDTO>> {
  @Override
  public String getQueryName() {
    return "GetUserAuthData.v1";
  }
}
