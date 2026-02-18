package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.ports.out.PasswordHasher;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginServiceMetricsTest {

  private QueryBus queryBus;
  private SessionRepository sessionRepository;
  private PasswordHasher passwordHasher;
  private TokenProvider tokenProvider;
  private RefreshTokenHasher refreshTokenHasher;
  private DomainEventPublisher eventPublisher;
  private SimpleMeterRegistry registry;
  private IdentityMetrics metrics;
  private LoginService loginService;

  @BeforeEach
  void setUp() {
    queryBus = mock(QueryBus.class);
    sessionRepository = mock(SessionRepository.class);
    passwordHasher = mock(PasswordHasher.class);
    tokenProvider = mock(TokenProvider.class);
    refreshTokenHasher = mock(RefreshTokenHasher.class);
    eventPublisher = mock(DomainEventPublisher.class);
    registry = new SimpleMeterRegistry();
    metrics = new IdentityMetrics(registry);

    loginService = new LoginService(queryBus, sessionRepository, passwordHasher, tokenProvider, 86400000L,
        Clock.fixed(Instant.now(), ZoneId.of("UTC")), refreshTokenHasher, eventPublisher, metrics);
  }

  @Test
  void successfulLoginIncrementsSuccessCounterAndSessionCreated() {
    UserAuthDataDTO userDTO = new UserAuthDataDTO(UUID.randomUUID(), "hashed", "ACTIVE");
    when(queryBus.execute(any())).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(passwordHasher.verify(any(), any())).thenReturn(true);
    when(tokenProvider.generateAccessToken(any())).thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
    when(refreshTokenHasher.hash(any())).thenReturn(new RefreshTokenHash("hashed-refresh"));

    loginService.login(new LoginCommand("user@example.com", "password", "device-1"));

    assertThat(registry.find("identity.auth.login.attempt").tag("outcome", "success").counter()).isNotNull();
    assertThat(registry.find("identity.auth.login.attempt").tag("outcome", "success").counter().count()).isEqualTo(1.0);
    assertThat(registry.find("identity.auth.session.created").counter()).isNotNull();
    assertThat(registry.find("identity.auth.session.created").counter().count()).isEqualTo(1.0);
  }

  @Test
	void userNotFoundIncrementsFailureWithUserNotFoundReason() {
		when(queryBus.execute(any())).thenReturn(new QueryResult.Success<>(Optional.empty()));

		assertThatThrownBy(() -> loginService.login(new LoginCommand("unknown@example.com", "password", "device-1")))
				.isInstanceOf(InvalidCredentialsException.class);

		assertThat(registry.find("identity.auth.login.attempt").tag("outcome", "failure").counter()).isNotNull();
		assertThat(registry.find("identity.auth.login.attempt").tag("outcome", "failure").counter().count())
				.isEqualTo(1.0);
		assertThat(registry.find("identity.auth.login.failure").tag("reason", "user_not_found").counter()).isNotNull();
		assertThat(registry.find("identity.auth.login.failure").tag("reason", "user_not_found").counter().count())
				.isEqualTo(1.0);
	}

  @Test
  void wrongPasswordIncrementsFailureWithInvalidCredentialsReason() {
    UserAuthDataDTO userDTO = new UserAuthDataDTO(UUID.randomUUID(), "hashed", "ACTIVE");
    when(queryBus.execute(any())).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(passwordHasher.verify(any(), any())).thenReturn(false);

    assertThatThrownBy(() -> loginService.login(new LoginCommand("user@example.com", "wrong", "device-1")))
        .isInstanceOf(InvalidCredentialsException.class);

    assertThat(registry.find("identity.auth.login.attempt").tag("outcome", "failure").counter()).isNotNull();
    assertThat(registry.find("identity.auth.login.failure").tag("reason", "invalid_credentials").counter()).isNotNull();
    assertThat(registry.find("identity.auth.login.failure").tag("reason", "invalid_credentials").counter().count())
        .isEqualTo(1.0);
  }
}
