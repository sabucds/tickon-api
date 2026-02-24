package com.tickon.identity.shared.platform.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentityMetricsTest {

  private SimpleMeterRegistry registry;
  private IdentityMetrics metrics;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    metrics = new IdentityMetrics(registry);
  }

  @Test
  void userRegisteredIncrementsCounter() {
    metrics.userRegistered().increment();
    Counter counter = registry.find("identity.user.registered").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void registrationFailedRecordsReason() {
    metrics.registrationFailed("duplicate_email").increment();
    Counter counter = registry.find("identity.user.registration.failed").tag("reason", "duplicate_email").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void userDeletedIncrementsCounter() {
    metrics.userDeleted().increment();
    Counter counter = registry.find("identity.user.deleted").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void loginAttemptRecordsOutcome() {
    metrics.loginAttempt("success").increment();
    Counter counter = registry.find("identity.auth.login.attempt").tag("outcome", "success").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void loginFailureRecordsReason() {
    metrics.loginFailure("invalid_credentials").increment();
    Counter counter = registry.find("identity.auth.login.failure").tag("reason", "invalid_credentials").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void sessionCreatedIncrementsCounter() {
    metrics.sessionCreated().increment();
    Counter counter = registry.find("identity.auth.session.created").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void sessionRevokedRecordsReason() {
    metrics.sessionRevoked("logout").increment();
    Counter counter = registry.find("identity.auth.session.revoked").tag("reason", "logout").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void tokenRefreshRecordsOutcome() {
    metrics.tokenRefresh("success").increment();
    Counter counter = registry.find("identity.auth.token.refresh").tag("outcome", "success").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void tokenRefreshFailureRecordsReason() {
    metrics.tokenRefreshFailure("expired").increment();
    Counter counter = registry.find("identity.auth.token.refresh.failure").tag("reason", "expired").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void logoutIncrementsCounter() {
    metrics.logout().increment();
    Counter counter = registry.find("identity.auth.logout").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void passwordResetRequestedIncrementsCounter() {
    metrics.passwordResetRequested().increment();
    Counter counter = registry.find("identity.auth.password_reset.requested").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void passwordResetCompletedIncrementsCounter() {
    metrics.passwordResetCompleted().increment();
    Counter counter = registry.find("identity.auth.password_reset.completed").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void passwordResetFailedRecordsReason() {
    metrics.passwordResetFailed("expired_token").increment();
    Counter counter = registry.find("identity.auth.password_reset.failed").tag("reason", "expired_token").counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void emailSentRecordsOutcomeAndType() {
    metrics.emailSent("success", "password_reset").increment();
    Counter counter = registry.find("identity.email.sent").tag("outcome", "success").tag("type", "password_reset")
        .counter();
    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }
}
