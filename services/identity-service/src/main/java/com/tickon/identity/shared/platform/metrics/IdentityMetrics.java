package com.tickon.identity.shared.platform.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class IdentityMetrics {

  private static final String TAG_OUTCOME = "outcome";
  private static final String TAG_REASON = "reason";
  private static final String TAG_TYPE = "type";

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

  public void registrationFailed(String reason) {
    Counter.builder("identity.user.registration.failed").tag(TAG_REASON, reason).register(registry).increment();
  }

  public Counter userDeleted() {
    return userDeletedCounter;
  }

  // --- Auth: Login / Session ---

  public void loginSuccess() {
    Counter.builder("identity.auth.login").tag(TAG_OUTCOME, "success").register(registry).increment();
  }

  public void loginFailure(String reason) {
    Counter.builder("identity.auth.login").tag(TAG_OUTCOME, "failure").tag(TAG_REASON, reason).register(registry)
        .increment();
  }

  public Counter sessionCreated() {
    return sessionCreatedCounter;
  }

  public void sessionRevoked(String reason) {
    Counter.builder("identity.auth.session.revoked").tag(TAG_REASON, reason).register(registry).increment();
  }

  public void tokenRefreshSuccess() {
    Counter.builder("identity.auth.token.refresh").tag(TAG_OUTCOME, "success").register(registry).increment();
  }

  public void tokenRefreshFailure(String reason) {
    Counter.builder("identity.auth.token.refresh").tag(TAG_OUTCOME, "failure").tag(TAG_REASON, reason).register(registry)
        .increment();
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

  public void passwordResetFailed(String reason) {
    Counter.builder("identity.auth.password_reset.failed").tag(TAG_REASON, reason).register(registry).increment();
  }

  // --- Infrastructure ---

  public void emailSent(String outcome, String type) {
    Counter.builder("identity.email.sent").tag(TAG_OUTCOME, outcome).tag(TAG_TYPE, type).register(registry).increment();
  }
}
