package com.tickon.identity.auth.application.ports.out;

import com.tickon.identity.auth.domain.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ResetTokenRepository {
  void save(PasswordResetToken token);

  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  void invalidateAllForUser(UUID userId, Instant now);
}
