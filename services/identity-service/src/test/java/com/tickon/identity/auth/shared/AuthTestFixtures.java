package com.tickon.identity.auth.shared;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.time.Duration;
import java.time.Instant;

public final class AuthTestFixtures {
  private AuthTestFixtures() {}

  public static final UserId DEFAULT_USER_ID = UserId.generate();

  public static AuthUser anAuthUser() {
    return new AuthUser(DEFAULT_USER_ID, PasswordHash.from("password-hash-abc"), UserStatus.ACTIVE);
  }

  public static AuthUser anAuthUser(UserId userId) {
    return new AuthUser(userId, PasswordHash.from("password-hash-abc"), UserStatus.ACTIVE);
  }

  public static Session aSession(Instant now, Duration duration) {
    return Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), DEFAULT_USER_ID, "device-123",
        FamilyId.generate(), null, duration, now);
  }

}