package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.EmailSender;
import com.tickon.identity.auth.domain.events.PasswordResetRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SendPasswordResetEmailHandler {

  private static final Logger log = LoggerFactory.getLogger(SendPasswordResetEmailHandler.class);
  private final EmailSender emailSender;

  public SendPasswordResetEmailHandler(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Async
  @EventListener
  public void handle(PasswordResetRequestedEvent event) {
    log.info("Sending password reset email to {}", event.email().value());
    try {
      String recipientName = event.email().value().split("@")[0];
      emailSender.sendPasswordResetEmail(event.email(), event.plainToken(), recipientName);
    } catch (Exception e) {
      log.error("Failed to send password reset email", e);
    }
  }
}
