package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;
import java.util.UUID;

/**
 * Query to get authentication-related user data. This contract is shared
 * between modules.
 *
 * @version 1.0
 * @since 2024-01-01
 */
public record GetUserAuthDataQuery(UUID userId) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserAuthData.v1";
  }
}
