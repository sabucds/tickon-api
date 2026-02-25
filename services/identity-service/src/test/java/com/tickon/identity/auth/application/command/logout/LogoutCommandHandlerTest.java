package com.tickon.identity.auth.application.command.logout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.auth.application.ports.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.events.SessionRevokedEvent;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
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
class LogoutCommandHandlerTest {

  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private RefreshTokenHasher refreshTokenHasher;
  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private LogoutCommandHandler handler;

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration sessionDuration = Duration.ofDays(14);

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String REFRESH_TOKEN_HASH = "hashed-refresh-token";

  @BeforeEach
  void setUp() {
    handler = new LogoutCommandHandler(sessionRepository, refreshTokenHasher, fixedClock, eventPublisher, metrics);
  }

  @Test
  void shouldRevokeSession_WhenValidRefreshToken() {
    Session session = createValidSession(USER_ID);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);

    handler.handle(new LogoutCommand(REFRESH_TOKEN));

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());

    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.isRevoked()).isTrue();
    assertThat(savedSession.revokeReason()).isEqualTo(RevokeReason.USER_LOGOUT);
    assertThat(savedSession.revokedAt()).isEqualTo(fixedInstant);
  }

  @Test
  void shouldNotThrowException_WhenRefreshTokenNotFound() {
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    when(sessionRepository.findByRefreshTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.empty());

    handler.handle(new LogoutCommand(REFRESH_TOKEN));

    verify(sessionRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldNotThrowException_WhenSessionAlreadyRevoked() {
    Session session = createRevokedSession(USER_ID);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);

    handler.handle(new LogoutCommand(REFRESH_TOKEN));

    verify(sessionRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldHashRefreshToken_BeforeLookup() {
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    when(sessionRepository.findByRefreshTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.empty());

    handler.handle(new LogoutCommand(REFRESH_TOKEN));

    verify(refreshTokenHasher).hash(REFRESH_TOKEN);
    verify(sessionRepository).findByRefreshTokenHash(REFRESH_TOKEN_HASH);
  }

  @Test
  void shouldPublishSessionRevokedEvent_WhenSessionRevoked() {
    Session session = createValidSession(USER_ID);
    stubRefreshTokenHash(REFRESH_TOKEN, REFRESH_TOKEN_HASH);
    stubSessionFound(REFRESH_TOKEN_HASH, session);

    handler.handle(new LogoutCommand(REFRESH_TOKEN));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventPublisher).publishAll(eventsCaptor.capture());

    List<DomainEvent> events = eventsCaptor.getValue();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(SessionRevokedEvent.class);

    SessionRevokedEvent event = (SessionRevokedEvent) events.get(0);
    assertThat(event.reason()).isEqualTo(RevokeReason.USER_LOGOUT);
  }

  private Session createValidSession(UUID userId) {
    Session session = Session.create(SessionId.generate(), RefreshTokenHash.from(REFRESH_TOKEN_HASH), userId,
        "device-123", FamilyId.generate(), null, sessionDuration, fixedInstant);
    session.clearEvents();
    return session;
  }

  private Session createRevokedSession(UUID userId) {
    Session session = createValidSession(userId);
    session.revoke(fixedInstant, RevokeReason.USER_LOGOUT);
    session.clearEvents();
    return session;
  }

  private void stubRefreshTokenHash(String token, String hash) {
    when(refreshTokenHasher.hash(token)).thenReturn(RefreshTokenHash.from(hash));
  }

  private void stubSessionFound(String hash, Session session) {
    when(sessionRepository.findByRefreshTokenHash(hash)).thenReturn(Optional.of(session));
  }
}
