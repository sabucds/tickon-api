package com.tickon.identity.auth.application.command.requestpasswordreset;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.application.ports.ResetTokenGenerator;
import com.tickon.identity.auth.application.ports.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.contracts.user.queries.GetUserByEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RequestPasswordResetCommandHandler
    implements CommandHandler<RequestPasswordResetCommand, RequestPasswordResetResult> {

  private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetCommandHandler.class);

  private final QueryBus queryBus;
  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final ResetTokenGenerator resetTokenGenerator;
  private final Duration tokenDuration;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public RequestPasswordResetCommandHandler(QueryBus queryBus, ResetTokenRepository resetTokenRepository,
      ResetTokenHasher resetTokenHasher, ResetTokenGenerator resetTokenGenerator,
      @Value("${security.password-reset.token-expiration-ms:3600000}") long tokenExpirationMs, Clock clock,
      DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.queryBus = queryBus;
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.resetTokenGenerator = resetTokenGenerator;
    this.tokenDuration = Duration.ofMillis(tokenExpirationMs);
    this.clock = clock;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  @Transactional
  public CommandResult<RequestPasswordResetResult> handle(RequestPasswordResetCommand command) {
    log.info("Password reset requested");
    metrics.passwordResetRequested().increment();

    Optional<UserAuthDataDTO> userOpt = queryBus.execute(new GetUserByEmailQuery(command.email())).orElseThrow();

    if (userOpt.isEmpty()) {
      return new CommandResult.Success<>(RequestPasswordResetResult.success());
    }

    UserAuthDataDTO userDTO = userOpt.get();
    UUID userId = userDTO.id();

    String plainToken = resetTokenGenerator.generateSecureToken();
    ResetTokenHash tokenHash = resetTokenHasher.hash(plainToken);
    Instant now = clock.instant();

    resetTokenRepository.invalidateAllForUser(userId, now);

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId, command.email(),
        tokenDuration, now, plainToken);

    resetTokenRepository.save(token);

    eventPublisher.publishAll(token.domainEvents());
    token.clearEvents();

    return new CommandResult.Success<>(RequestPasswordResetResult.success());
  }

  @Override
  public Class<RequestPasswordResetCommand> getCommandClass() {
    return RequestPasswordResetCommand.class;
  }
}
