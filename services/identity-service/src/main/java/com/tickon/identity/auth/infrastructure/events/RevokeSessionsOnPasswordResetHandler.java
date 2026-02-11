package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.events.PasswordResetCompletedEvent;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
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

  public RevokeSessionsOnPasswordResetHandler(SessionRepository sessionRepository, Clock clock) {
    this.sessionRepository = sessionRepository;
    this.clock = clock;
  }

  @Async
  @EventListener
  public void handle(PasswordResetCompletedEvent event) {
    log.info("Revoking all sessions for user {} due to password reset", event.userId().value());
    sessionRepository.revokeAllByUserId(event.userId(), clock.instant(), RevokeReason.PASSWORD_RESET);
  }
}
