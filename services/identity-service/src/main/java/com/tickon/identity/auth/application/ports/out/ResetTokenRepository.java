package com.tickon.identity.auth.application.ports.out;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;

public interface ResetTokenRepository {
  void save(PasswordResetToken token);

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  void invalidateAllForUser(UserId userId, Instant now);
}
