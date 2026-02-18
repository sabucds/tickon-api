package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;
import com.tickon.identity.auth.application.ports.in.VerifyResetTokenUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VerifyResetTokenService implements VerifyResetTokenUseCase {

  private static final Logger log = LoggerFactory.getLogger(VerifyResetTokenService.class);

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
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());

    return resetTokenRepository.findByTokenHash(tokenHash.value()).map(token -> {
      Instant now = clock.instant();
      boolean valid = !token.isExpired(now) && !token.isUsed();
      log.debug("Reset token verification: valid={}", valid);
      return new VerifyResetTokenResult(valid);
    }).orElseGet(() -> {
      log.debug("Reset token verification: token not found");
      return new VerifyResetTokenResult(false);
    });
  }
}
