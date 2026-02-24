package com.tickon.identity.auth.application.command.refresh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.LoginResult;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.events.SessionCreatedEvent;
import com.tickon.identity.auth.domain.events.SessionRevokedEvent;
import com.tickon.identity.auth.domain.exceptions.InvalidRefreshTokenException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import com.tickon.identity.contracts.user.queries.GetUserAuthDataQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
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
class RefreshTokenCommandHandlerTest {

  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private QueryBus queryBus;
  @Mock
  private TokenProvider tokenProvider;
  @Mock
  private RefreshTokenHasher refreshTokenHasher;
  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private RefreshTokenCommandHandler handler;

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration sessionDuration = Duration.ofDays(14);

  private static final UserId USER_ID = UserId.generate();
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String REFRESH_TOKEN_HASH = "hashed-refresh-token";
  private static final String NEW_ACCESS_TOKEN = "new-access-token";
  private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
  private static final String NEW_REFRESH_TOKEN_HASH = "new-hashed-refresh-token";

  @BeforeEach
  void setUp() {
    handler = new RefreshTokenCommandHandler(sessionRepository, queryBus, tokenProvider, refreshTokenHasher, fixedClock,
        eventPublisher, metrics);
  }

  @Test
  void shouldRotateToken_WhenValidRefreshToken() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createValidSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);
    stubUserFound(user);
    stubNewTokens(user);

    LoginResult result = handler.handle(new RefreshTokenCommand(REFRESH_TOKEN)).orElseThrow();

    assertThat(result.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
    assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
  }

  @Test
  void shouldThrowException_WhenRefreshTokenNotFound() {
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    when(sessionRepository.findByRefreshTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand(REFRESH_TOKEN)))
        .isInstanceOf(InvalidRefreshTokenException.class).hasMessageContaining("Invalid refresh token");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldRevokeEntireFamily_WhenRevokedTokenIsReused() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createRevokedSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);

    assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand(REFRESH_TOKEN)))
        .isInstanceOf(InvalidRefreshTokenException.class).hasMessageContaining("Invalid refresh token");

    verify(sessionRepository).revokeAllByFamilyId(session.familyId(), fixedInstant, RevokeReason.TOKEN_REUSE_DETECTED);
    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrowException_WhenSessionIsExpired() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createExpiredSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);

    assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand(REFRESH_TOKEN)))
        .isInstanceOf(InvalidRefreshTokenException.class).hasMessageContaining("Invalid refresh token");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrowException_WhenUserNotFound() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createValidSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);
    when(queryBus.execute(any(GetUserAuthDataQuery.class))).thenReturn(new QueryResult.Success<>(Optional.empty()));

    assertThatThrownBy(() -> handler.handle(new RefreshTokenCommand(REFRESH_TOKEN)))
        .isInstanceOf(InvalidRefreshTokenException.class).hasMessageContaining("Invalid refresh token");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldSaveBothSessions_WhenRotating() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createValidSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);
    stubUserFound(user);
    stubNewTokens(user);

    handler.handle(new RefreshTokenCommand(REFRESH_TOKEN));

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository, times(2)).save(sessionCaptor.capture());

    Session savedOldSession = sessionCaptor.getAllValues().get(0);
    Session savedNewSession = sessionCaptor.getAllValues().get(1);

    assertThat(savedOldSession.isRevoked()).isTrue();
    assertThat(savedOldSession.revokeReason()).isEqualTo(RevokeReason.SESSION_ROTATED);
    assertThat(savedNewSession.isRevoked()).isFalse();
    assertThat(savedNewSession.refreshTokenHash()).isEqualTo(RefreshTokenHash.from(NEW_REFRESH_TOKEN_HASH));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventPublisher, times(2)).publishAll(eventsCaptor.capture());

    List<List<DomainEvent>> allEvents = eventsCaptor.getAllValues();
    assertThat(allEvents.get(0)).hasSize(1);
    assertThat(allEvents.get(0).get(0)).isInstanceOf(SessionRevokedEvent.class);
    assertThat(allEvents.get(1)).hasSize(1);
    assertThat(allEvents.get(1).get(0)).isInstanceOf(SessionCreatedEvent.class);
  }

  @Test
  void shouldPreserveFamilyId_WhenRotating() {
    AuthUser user = AuthTestFixtures.anAuthUser(USER_ID);
    Session session = createValidSession(user);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);
    stubUserFound(user);
    stubNewTokens(user);

    handler.handle(new RefreshTokenCommand(REFRESH_TOKEN));

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository, times(2)).save(sessionCaptor.capture());

    Session savedOldSession = sessionCaptor.getAllValues().get(0);
    Session savedNewSession = sessionCaptor.getAllValues().get(1);

    assertThat(savedNewSession.familyId()).isEqualTo(savedOldSession.familyId());
    assertThat(savedNewSession.rotatedFromSessionId()).isEqualTo(savedOldSession.id());
  }

  private Session createValidSession(AuthUser user) {
    Session session = Session.create(SessionId.generate(), RefreshTokenHash.from(REFRESH_TOKEN_HASH), user.id(),
        "device-123", FamilyId.generate(), null, sessionDuration, fixedInstant);
    session.clearEvents();
    return session;
  }

  private Session createRevokedSession(AuthUser user) {
    Session session = createValidSession(user);
    session.revoke(fixedInstant, RevokeReason.USER_LOGOUT);
    return session;
  }

  private Session createExpiredSession(AuthUser user) {
    Instant pastInstant = fixedInstant.minus(Duration.ofDays(15));
    return Session.create(SessionId.generate(), RefreshTokenHash.from(REFRESH_TOKEN_HASH), user.id(), "device-123",
        FamilyId.generate(), null, sessionDuration, pastInstant);
  }

  private void stubRefreshTokenHash(String token, String hash) {
    when(refreshTokenHasher.hash(token)).thenReturn(RefreshTokenHash.from(hash));
  }

  private void stubSessionFound(String hash, Session session) {
    when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));
  }

  private void stubUserFound(AuthUser user) {
    UserAuthDataDTO dto = new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
    when(queryBus.execute(any(GetUserAuthDataQuery.class))).thenReturn(new QueryResult.Success<>(Optional.of(dto)));
  }

  private void stubNewTokens(AuthUser user) {
    when(tokenProvider.generateAccessToken(any(AuthUser.class))).thenReturn(NEW_ACCESS_TOKEN);
    when(tokenProvider.generateRefreshToken(any(AuthUser.class))).thenReturn(NEW_REFRESH_TOKEN);
    when(refreshTokenHasher.hash(NEW_REFRESH_TOKEN)).thenReturn(RefreshTokenHash.from(NEW_REFRESH_TOKEN_HASH));
  }
}
