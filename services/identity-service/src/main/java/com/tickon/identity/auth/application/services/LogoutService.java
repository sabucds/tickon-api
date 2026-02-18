package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.LogoutCommand;
import com.tickon.identity.auth.application.ports.in.LogoutUseCase;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService implements LogoutUseCase {

  private static final Logger log = LoggerFactory.getLogger(LogoutService.class);

  private final SessionRepository sessionRepository;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public LogoutService(SessionRepository sessionRepository, RefreshTokenHasher refreshTokenHasher, Clock clock,
      DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.sessionRepository = sessionRepository;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  @Transactional
  public void logout(LogoutCommand command) {
    RefreshTokenHash tokenHash = refreshTokenHasher.hash(command.refreshToken());
    sessionRepository.findByRefreshTokenHash(tokenHash.value()).ifPresent(this::revokeSession);
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
