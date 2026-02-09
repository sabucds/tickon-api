package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;

/**
 * Query to get authentication-related user data by username or email. This
 * contract is shared between modules.
 *
 * @version 1.0
 * @since 2024-01-01
 */
public record GetUserByUsernameOrEmailQuery(String usernameOrEmail) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserByUsernameOrEmail.v1";
  }
}
