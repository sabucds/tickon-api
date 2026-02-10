package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;
import com.tickon.identity.auth.application.ports.in.VerifyResetTokenUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class VerifyResetTokenService implements VerifyResetTokenUseCase {

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final Clock clock;

  public VerifyResetTokenService(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher,
      Clock clock) {
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.clock = clock;
  }

  @Override
  public VerifyResetTokenResult verifyResetToken(VerifyResetTokenCommand command) {
    // Hash token
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());

    // Find token
    return resetTokenRepository.findByTokenHash(tokenHash.value()).map(token -> {
      Instant now = clock.instant();
      // Check if valid: not expired and not used
      boolean valid = !token.isExpired(now) && !token.isUsed();
      return new VerifyResetTokenResult(valid);
    }).orElse(new VerifyResetTokenResult(false)); // Token not found = invalid
  }
}
