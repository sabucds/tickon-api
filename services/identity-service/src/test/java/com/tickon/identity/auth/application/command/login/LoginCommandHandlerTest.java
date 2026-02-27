package com.tickon.identity.auth.application.command.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.ports.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.SessionRepository;
import com.tickon.identity.auth.application.ports.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.events.SessionCreatedEvent;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import com.tickon.identity.contracts.user.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.exceptions.IdentityExceptionCodes;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginCommandHandlerTest {

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

  private LoginCommandHandler handler;

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final long sessionExpirationMS = 1209600000;

  private static final String IDENTIFIER = "john@example.com";
  private static final String DEVICE_ID = "device-123";

  @BeforeEach
  void setUp() {
    handler = new LoginCommandHandler(queryBus, sessionRepository, passwordHasher, tokenProvider, sessionExpirationMS,
        fixedClock, refreshTokenHasher, eventPublisher, metrics);
  }

  @Test
  void shouldLoginAndPersistSession_WhenCredentialsAreValid() {
    UserAuthDataDTO user = AuthTestFixtures.aUserAuthData();
    UUID userId = user.id();
    stubUserFound(user);
    stubValidPassword("plain-password", user);
    stubTokens("access-token", "refresh-token", "hashed-refresh-token");

    LoginResult result = handler.handle(new LoginCommand(IDENTIFIER, "plain-password", DEVICE_ID)).orElseThrow();

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());

    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.userId()).isEqualTo(userId);
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

    assertThatThrownBy(() -> handler.handle(new LoginCommand("missing", "any", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage(IdentityExceptionCodes.INVALID_CREDENTIALS.name());

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenPasswordIsInvalid() {
    UserAuthDataDTO user = AuthTestFixtures.aUserAuthData();
    stubUserFound(user);
    stubInvalidPassword("wrong-password", user);

    assertThatThrownBy(() -> handler.handle(new LoginCommand(IDENTIFIER, "wrong-password", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class).hasMessage(IdentityExceptionCodes.INVALID_CREDENTIALS.name());

    verify(sessionRepository, never()).save(any());
  }

  private void stubUserFound(UserAuthDataDTO user) {
    when(queryBus.execute(any(GetUserByUsernameOrEmailQuery.class)))
        .thenReturn(new QueryResult.Success<>(Optional.of(user)));
  }

  private void stubValidPassword(String raw, UserAuthDataDTO user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(true);
  }

  private void stubInvalidPassword(String raw, UserAuthDataDTO user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(false);
  }

  private void stubTokens(String access, String refresh, String refreshHash) {
    when(tokenProvider.generateAccessToken(any(UUID.class))).thenReturn(access);
    when(tokenProvider.generateRefreshToken()).thenReturn(refresh);
    when(refreshTokenHasher.hash(refresh)).thenReturn(RefreshTokenHash.from(refreshHash));
  }
}
