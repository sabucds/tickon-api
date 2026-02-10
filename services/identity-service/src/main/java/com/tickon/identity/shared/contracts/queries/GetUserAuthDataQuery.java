package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;
import java.util.UUID;

public record GetUserAuthDataQuery(UUID userId) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserAuthData.v1";
  }
}
