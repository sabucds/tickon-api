package com.tickon.identity.auth.application.command.logout;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.identity.auth.application.ports.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogoutCommandHandler implements CommandHandler<LogoutCommand, Void> {

  private static final Logger log = LoggerFactory.getLogger(LogoutCommandHandler.class);

  private final SessionRepository sessionRepository;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public LogoutCommandHandler(SessionRepository sessionRepository, RefreshTokenHasher refreshTokenHasher, Clock clock,
      DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.sessionRepository = sessionRepository;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  @Transactional
  public CommandResult<Void> handle(LogoutCommand command) {
    RefreshTokenHash tokenHash = refreshTokenHasher.hash(command.refreshToken());
    sessionRepository.findByRefreshTokenHash(tokenHash.value()).ifPresent(this::revokeSession);
    return new CommandResult.Success<>(null);
  }

  @Override
  public Class<LogoutCommand> getCommandClass() {
    return LogoutCommand.class;
  }

  private void revokeSession(Session session) {
    if (session.isRevoked()) {
      return;
    }

    Instant now = clock.instant();
    session.revoke(now, RevokeReason.USER_LOGOUT);
    sessionRepository.save(session);
    eventPublisher.publishAll(session.domainEvents());
    session.clearEvents();

    log.info("Logout: sessionId={} revoked", session.id().value());
    metrics.logout().increment();
    metrics.sessionRevoked("logout").increment();
  }
}
