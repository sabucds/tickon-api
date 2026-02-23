package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.events.SessionCreatedEvent;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import com.tickon.identity.contracts.user.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.out.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock
  private QueryBus queryBus;
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private PasswordHasher passwordHasher;
  @Mock
  private TokenProvider tokenProvider;
  @Mock
  private RefreshTokenHasher refreshTokenHasher;
  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private LoginService loginService;

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final long sessionExpirationMS = 1209600000;

  private static final String IDENTIFIER = "john@example.com";
  private static final String DEVICE_ID = "device-123";

  @BeforeEach
  void setUp() {
    loginService = new LoginService(queryBus, sessionRepository, passwordHasher, tokenProvider, sessionExpirationMS,
        fixedClock, refreshTokenHasher, eventPublisher, metrics);
  }

  @Test
  void shouldLoginAndPersistSession_WhenCredentialsAreValid() {
    AuthUser user = AuthTestFixtures.anAuthUser();
    stubUserFound(user);
    stubValidPassword("plain-password", user);
    stubTokens(user, "access-token", "refresh-token", "hashed-refresh-token");

    LoginResult result = loginService.login(new LoginCommand(IDENTIFIER, "plain-password", DEVICE_ID));

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());

    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.userId()).isEqualTo(user.id());
    assertThat(savedSession.deviceId()).isEqualTo(DEVICE_ID);
    assertThat(savedSession.refreshTokenHash()).isEqualTo(RefreshTokenHash.from("hashed-refresh-token"));
    assertThat(savedSession.isRevoked()).isFalse();
    assertThat(savedSession.absoluteExpiresAt()).isEqualTo(fixedInstant.plus(Duration.ofMillis(sessionExpirationMS)));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventPublisher).publishAll(eventsCaptor.capture());

    List<DomainEvent> publishedEvents = eventsCaptor.getValue();
    assertThat(publishedEvents).hasSize(1);
    assertThat(publishedEvents.get(0)).isInstanceOf(SessionCreatedEvent.class);
  }

  @Test
  void shouldThrow_WhenUserNotFound() {
    when(queryBus.execute(any(GetUserByUsernameOrEmailQuery.class)))
        .thenReturn(new QueryResult.Success<>(Optional.empty()));

    assertThatThrownBy(() -> loginService.login(new LoginCommand("missing", "any", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenPasswordIsInvalid() {
    AuthUser user = AuthTestFixtures.anAuthUser();
    stubUserFound(user);
    stubInvalidPassword("wrong-password", user);

    assertThatThrownBy(() -> loginService.login(new LoginCommand(IDENTIFIER, "wrong-password", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }

  private void stubUserFound(AuthUser user) {
    UserAuthDataDTO dto = new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
    when(queryBus.execute(any(GetUserByUsernameOrEmailQuery.class)))
        .thenReturn(new QueryResult.Success<>(Optional.of(dto)));
  }

  private void stubValidPassword(String raw, AuthUser user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(true);
  }

  private void stubInvalidPassword(String raw, AuthUser user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(false);
  }

  private void stubTokens(AuthUser user, String access, String refresh, String refreshHash) {
    when(tokenProvider.generateAccessToken(any(AuthUser.class))).thenReturn(access);
    when(tokenProvider.generateRefreshToken(any(AuthUser.class))).thenReturn(refresh);
    when(refreshTokenHasher.hash(refresh)).thenReturn(RefreshTokenHash.from(refreshHash));
  }
}
