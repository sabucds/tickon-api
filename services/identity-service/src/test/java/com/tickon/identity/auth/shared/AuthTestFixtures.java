package com.tickon.identity.auth.shared;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class AuthTestFixtures {
  private AuthTestFixtures() {}

  public static final UUID DEFAULT_USER_ID = UUID.randomUUID();

  public static UserAuthDataDTO aUserAuthData() {
    return aUserAuthData(DEFAULT_USER_ID);
  }

  public static UserAuthDataDTO aUserAuthData(UUID userId) {
    return new UserAuthDataDTO(userId, "password-hash-abc", "ACTIVE");
  }

  public static Session aSession(Instant now, Duration duration) {
    return Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), DEFAULT_USER_ID, "device-123",
        FamilyId.generate(), null, duration, now);
  }

}
