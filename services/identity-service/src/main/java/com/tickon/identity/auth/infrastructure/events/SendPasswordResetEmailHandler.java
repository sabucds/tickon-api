package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.EmailSender;
import com.tickon.identity.auth.domain.events.PasswordResetRequestedEvent;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SendPasswordResetEmailHandler {

  private static final Logger log = LoggerFactory.getLogger(SendPasswordResetEmailHandler.class);
  private final EmailSender emailSender;
  private final IdentityMetrics metrics;

  public SendPasswordResetEmailHandler(EmailSender emailSender, IdentityMetrics metrics) {
    this.emailSender = emailSender;
    this.metrics = metrics;
  }

  @Async
  @EventListener
  public void handle(PasswordResetRequestedEvent event) {
    log.info("Sending password reset email");
    try {
      String recipientName = event.email().split("@")[0];
      emailSender.sendPasswordResetEmail(event.email(), event.plainToken(), recipientName);
      metrics.emailSent("success", "password_reset").increment();
    } catch (Exception e) {
      log.error("Failed to send password reset email", e);
      metrics.emailSent("failure", "password_reset").increment();
    }
  }
}
