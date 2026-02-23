package com.tickon.identity.auth.application.services;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.application.dto.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.dto.RequestPasswordResetResult;
import com.tickon.identity.auth.application.ports.in.RequestPasswordResetUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenGenerator;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.contracts.user.queries.GetUserByEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

  private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetService.class);

  private final QueryBus queryBus;
  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final ResetTokenGenerator resetTokenGenerator;
  private final Duration tokenDuration;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public RequestPasswordResetService(QueryBus queryBus, ResetTokenRepository resetTokenRepository,
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
  public RequestPasswordResetResult requestPasswordReset(RequestPasswordResetCommand command) {
    log.info("Password reset requested");
    metrics.passwordResetRequested().increment();

    Optional<UserAuthDataDTO> userOpt = queryBus.execute(new GetUserByEmailQuery(command.email().value()))
        .orElseThrow();

    if (userOpt.isEmpty()) {
      return RequestPasswordResetResult.success();
    }

    UserAuthDataDTO userDTO = userOpt.get();
    UserId userId = new UserId(userDTO.id());

    String plainToken = resetTokenGenerator.generateSecureToken();
    ResetTokenHash tokenHash = resetTokenHasher.hash(plainToken);
    Instant now = clock.instant();

    resetTokenRepository.invalidateAllForUser(userId, now);

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId, command.email(),
        tokenDuration, now, plainToken);

    resetTokenRepository.save(token);

    eventPublisher.publishAll(token.domainEvents());
    token.clearEvents();

    return RequestPasswordResetResult.success();
  }
}
