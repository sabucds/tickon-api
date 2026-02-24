package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.events.PasswordResetCompletedEvent;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RevokeSessionsOnPasswordResetHandler {

  private static final Logger log = LoggerFactory.getLogger(RevokeSessionsOnPasswordResetHandler.class);
  private final SessionRepository sessionRepository;
  private final Clock clock;
  private final IdentityMetrics metrics;

  public RevokeSessionsOnPasswordResetHandler(SessionRepository sessionRepository, Clock clock,
      IdentityMetrics metrics) {
    this.sessionRepository = sessionRepository;
    this.clock = clock;
    this.metrics = metrics;
  }

  @Async
  @EventListener
  public void handle(PasswordResetCompletedEvent event) {
    log.info("Revoking all sessions for user {} due to password reset", event.userId());
    try {
      sessionRepository.revokeAllByUserId(event.userId(), clock.instant(), RevokeReason.PASSWORD_RESET);
      metrics.sessionRevoked("password_reset").increment();
    } catch (Exception e) {
      log.error("Failed to revoke sessions for user {} after password reset", event.userId(), e);
    }
  }
}
