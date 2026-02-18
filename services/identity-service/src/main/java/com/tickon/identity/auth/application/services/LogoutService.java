package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.LogoutCommand;
import com.tickon.identity.auth.application.ports.in.LogoutUseCase;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService implements LogoutUseCase {

  private final SessionRepository sessionRepository;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;

  public LogoutService(SessionRepository sessionRepository, RefreshTokenHasher refreshTokenHasher, Clock clock,
      DomainEventPublisher eventPublisher) {
    this.sessionRepository = sessionRepository;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
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
  }
}
