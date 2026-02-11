package com.tickon.identity.auth.application.services;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.dto.ResetPasswordCommand;
import com.tickon.identity.auth.application.ports.in.ResetPasswordUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.exceptions.InvalidResetTokenException;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.shared.contracts.commands.ChangePasswordCommand;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordService implements ResetPasswordUseCase {

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final Clock clock;
  private final CommandBus commandBus;
  private final DomainEventPublisher eventPublisher;

  public ResetPasswordService(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher, Clock clock,
      CommandBus commandBus, DomainEventPublisher eventPublisher) {
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.clock = clock;
    this.commandBus = commandBus;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordCommand command) {
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());
    PasswordResetToken token = resetTokenRepository.findByTokenHash(tokenHash.value())
        .orElseThrow(InvalidResetTokenException::new);

    Instant now = clock.instant();

    if (token.isExpired(now)) {
      throw new InvalidResetTokenException();
    }

    if (token.isUsed()) {
      throw new InvalidResetTokenException();
    }

    UserId userId = token.userId();
    commandBus.execute(new ChangePasswordCommand(userId, command.newPassword()));

    token.markAsUsed(now);
    resetTokenRepository.save(token);

    eventPublisher.publishAll(token.domainEvents());
    token.clearEvents();
  }
}
