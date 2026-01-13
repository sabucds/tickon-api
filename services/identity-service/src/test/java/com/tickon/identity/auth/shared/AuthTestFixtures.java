package com.tickon.identity.auth.shared;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.time.Duration;
import java.time.Instant;

public final class AuthTestFixtures {
  private AuthTestFixtures() {}

  public static User aUser() {
    return User.create(UserId.generate(), Email.from("john@example.com"), Username.from("johnny_doe"), "John", "Doe",
        new PasswordHash("hash"));
  }

  public static Session aSession(Instant now, Duration duration) {
    return Session.create(SessionId.generate(), RefreshTokenHash.from("sample-hash"), UserId.generate(), "device-123",
        FamilyId.generate(), null, now, duration);
  }

}