package com.tickon.identity.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class IdentityMetrics {

  private final MeterRegistry registry;

  // Tag-less counters pre-registered so they appear in /actuator/prometheus
  // before the first event
  private final Counter userRegisteredCounter;
  private final Counter userDeletedCounter;
  private final Counter sessionCreatedCounter;
  private final Counter logoutCounter;
  private final Counter passwordResetRequestedCounter;
  private final Counter passwordResetCompletedCounter;

  public IdentityMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.userRegisteredCounter = Counter.builder("identity.user.registered")
        .description("Total successful user registrations").register(registry);
    this.userDeletedCounter = Counter.builder("identity.user.deleted").description("Total users deleted")
        .register(registry);
    this.sessionCreatedCounter = Counter.builder("identity.auth.session.created").description("Total sessions created")
        .register(registry);
    this.logoutCounter = Counter.builder("identity.auth.logout").description("Total logout operations")
        .register(registry);
    this.passwordResetRequestedCounter = Counter.builder("identity.auth.password_reset.requested")
        .description("Total password reset requests").register(registry);
    this.passwordResetCompletedCounter = Counter.builder("identity.auth.password_reset.completed")
        .description("Total password resets completed").register(registry);
  }

  // --- User ---

  public Counter userRegistered() {
    return userRegisteredCounter;
  }

  public Counter registrationFailed(String reason) {
    return Counter.builder("identity.user.registration.failed").tag("reason", reason).register(registry);
  }

  public Counter userDeleted() {
    return userDeletedCounter;
  }

  // --- Auth: Login / Session ---

  public Counter loginAttempt(String outcome) {
    return Counter.builder("identity.auth.login.attempt").tag("outcome", outcome).register(registry);
  }

  public Counter loginFailure(String reason) {
    return Counter.builder("identity.auth.login.failure").tag("reason", reason).register(registry);
  }

  public Counter sessionCreated() {
    return sessionCreatedCounter;
  }

  public Counter sessionRevoked(String reason) {
    return Counter.builder("identity.auth.session.revoked").tag("reason", reason).register(registry);
  }

  public Counter tokenRefresh(String outcome) {
    return Counter.builder("identity.auth.token.refresh").tag("outcome", outcome).register(registry);
  }

  public Counter tokenRefreshFailure(String reason) {
    return Counter.builder("identity.auth.token.refresh.failure").tag("reason", reason).register(registry);
  }

  public Counter logout() {
    return logoutCounter;
  }

  // --- Auth: Password Reset ---

  public Counter passwordResetRequested() {
    return passwordResetRequestedCounter;
  }

  public Counter passwordResetCompleted() {
    return passwordResetCompletedCounter;
  }

  public Counter passwordResetFailed(String reason) {
    return Counter.builder("identity.auth.password_reset.failed").tag("reason", reason).register(registry);
  }

  // --- Infrastructure ---

  public Counter emailSent(String outcome, String type) {
    return Counter.builder("identity.email.sent").tag("outcome", outcome).tag("type", type).register(registry);
  }
}
