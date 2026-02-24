package com.tickon.identity.auth.application.command.resetpassword;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.exceptions.InvalidResetTokenException;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.contracts.user.commands.ChangePasswordCommand;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResetPasswordCommandHandler implements CommandHandler<ResetPasswordCommand, Void> {

  private static final Logger log = LoggerFactory.getLogger(ResetPasswordCommandHandler.class);

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final Clock clock;
  private final CommandBus commandBus;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public ResetPasswordCommandHandler(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher,
      Clock clock, CommandBus commandBus, DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.clock = clock;
    this.commandBus = commandBus;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  @Transactional
  public CommandResult<Void> handle(ResetPasswordCommand command) {
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());
    PasswordResetToken token = resetTokenRepository.findByTokenHash(tokenHash.value()).orElseThrow(() -> {
      log.warn("Password reset failed: token not found");
      metrics.passwordResetFailed("invalid_token").increment();
      return new InvalidResetTokenException();
    });

    Instant now = clock.instant();

    if (token.isExpired(now)) {
      log.warn("Password reset failed: token expired for userId={}", token.userId().value());
      metrics.passwordResetFailed("expired_token").increment();
      throw new InvalidResetTokenException();
    }

    if (token.isUsed()) {
      log.warn("Password reset failed: token already used for userId={}", token.userId().value());
      metrics.passwordResetFailed("invalid_token").increment();
      throw new InvalidResetTokenException();
    }

    UserId userId = token.userId();
    commandBus.execute(new ChangePasswordCommand(userId, command.newPassword())).orElseThrow();

    token.markAsUsed(now);
    resetTokenRepository.save(token);

    eventPublisher.publishAll(token.domainEvents());
    token.clearEvents();

    log.info("Password reset completed: userId={}", userId.value());
    metrics.passwordResetCompleted().increment();

    return new CommandResult.Success<>(null);
  }

  @Override
  public Class<ResetPasswordCommand> getCommandClass() {
    return ResetPasswordCommand.class;
  }
}
